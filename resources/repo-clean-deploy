set -ex
name="repo-clean"
group="scot/mygov/pipeline"
path="${group}/${name}/1.0.${PROMOTED_ID}/${name}-1.0.${PROMOTED_ID}.deb"
repo=http://nexus/repository/releases/
curl -sSf -o "${name}.deb" "${repo}/${path}"
sudo dpkg -i "${name}.deb"
