job("mygov-sitefail-trigger") {
    scm {
        git('ssh://git@stash.digital.gov.uk:7999/mgv/aws.git')
    }
    steps {
        shell('''\
set -e
cd tools/management/
echo ./aws_sitefail_trigger.sh \
            ''')
    }
}
