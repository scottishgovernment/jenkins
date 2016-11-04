set -ex
name="pipeline"
path="scot/mygov/pipeline/pipeline/1.0.${PROMOTED_ID}/pipeline-1.0.${PROMOTED_ID}.deb"
repo=http://nexus/content/repositories/releases/
curl -sSf -o "${name}.deb" "${repo}/${path}"
sudo dpkg -i "${name}.deb"
