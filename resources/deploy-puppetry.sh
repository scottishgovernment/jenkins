set -e
version=1.0.${BUILD_ID}
pipeline deploy:puppetry,${version},dev
pipeline deploy:puppetry,${version},dgv
