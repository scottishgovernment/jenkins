#!/bin/sh
set -e

case "${runtype}" in
  
    'dry-run')
        /usr/bin/repo-clean --dry-run
        ;;

    'run')
        /usr/bin/repo-clean
        ;;

    *)
        echo "Unrecognised runtype ${runtype}"
        exit 1
        ;;
esac

exit 0

# eof
