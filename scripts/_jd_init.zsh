#!/usr/bin/env zsh

CLASS_DIR=~/.local/share/jdcli/classes

alias jd=$(dirname "$0")/jd

_jd_list() {
    cur_rep="$1"
    grep -i "^$cur_rep" "$CLASS_DIR/.index" | grep -i -e "^$cur_rep\(.[^\.]*\)\?$"
}

_jd() {
    reply=("${(@f)$(_jd_list "$1")}")
}

compctl -q -S '.' -U -K _jd jd
