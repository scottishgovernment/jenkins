package build

import static build.Utils.repo
import static build.Utils.trim

static def buildStandalone(dsl) {
    dsl.job("devnet-ami") {
        displayName("Build Devnet AMI")
        steps {
            def script = StringBuilder.newInstance()
            script << trim('''\
                set -e
                set +x
                echo "Assuming AmiBuild role..."
                ROLE=$(aws sts assume-role --role-arn arn:aws:iam::381491968240:role/AmiBuild --role-session-name jenkins-session)
                export AWS_ACCESS_KEY_ID=$(printf '%s' "$ROLE" | jq -r '.Credentials.AccessKeyId')
                export AWS_SECRET_ACCESS_KEY=$(printf '%s' "$ROLE" | jq -r '.Credentials.SecretAccessKey')
                export AWS_SESSION_TOKEN=$(printf '%s' "$ROLE" | jq -r '.Credentials.SessionToken')
                PARAMETER_NAME="/devnet/latestAmiId"
                aws sts get-caller-identity
                echo "Building Devnet AMI..."
                repo=packer
                site=services
                version=services-\${override:-\$BUILD_ID}
           ''')
            script << trim('''\
                git clean -fdx
                git update-ref --no-deref HEAD HEAD
                git tag -a -m "Build ${version}" ${version}
                git push --tags origin "${version}"

                export PACKER_NO_COLOR=true
                packer validate \\
                    -var ami_name=${site}-${override:-$BUILD_ID} \\
                    -var site=${site} \\
                    templates/aws-devnet.json

                status_file=$(mktemp)
                trap 'rm -f "$status_file"' 0
                (packer build \
                    -machine-readable \\
                    -var ami_name=${site}-${override:-$BUILD_ID} \\
                    -var site=${site} \\
                    templates/aws-devnet.json || \\
                    printf "$?" > "$status_file") | \\
                tee build.log
                status="$(cat "$status_file")"
                if [ -n "$status" ]; then
                    exit "$status"
                fi

                ami_id=$(awk -F, '$5=="id" {sub("[a-z0-9-]*:", "", $6); print $6; exit;}' build.log)

                echo '{}' | jq ".artifactId=\\"${site}\\"
                  | .name=\\"${site}-${BUILD_ID}\\"
                  | .version=${BUILD_ID}
                  | .ami=\\"${ami_id}\\"" > ami.json
                
                aws ssm put-parameter \
                  --name "$PARAMETER_NAME" \
                  --type String \
                  --value "$ami_id" \
                  --overwrite \
                  --region eu-west-1

                echo "Stored AMI ID in Parameter Store: $PARAMETER_NAME = $ami_id"

            ''')
            shell(script.toString())
        }
        properties {
             promotions {
                  promotion {
                       name("Default")
                       icon("star-blue")
                       conditions {
                            selfPromotion(false)
                       }
                  }
             }
        }
        publishers {
            buildDescription('', "services-\${BUILD_ID}")
        }
        scm {
            git {
                remote {
                    name('origin')
                    url(repo('packer'))
                }
                branch('refs/heads/master')
            }
        }
    }
}
