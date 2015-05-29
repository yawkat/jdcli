#!/usr/bin/env zsh

_index=~/.local/share/jdcli/classes/.index

alias jd=$(dirname "$0")/jd

_jd_list_pkg() {
    grep "^$1" "$_index" | grep -e "^$1\(.[^\.]*\)#$" | sed 's/#$//'
}

_jd_list_cls() {
    full=$(sed 's/\./\\./g' <<< "$1")
    pkg=$(sed 's/^\(.*\\\.\).*$/\1/' <<< "$full")
    grep "^$pkg" "$_index" | grep -i -e "^$full"'[^\.#]*$'
}

_jd_pkg() {
    reply=("${(@f)$(_jd_list_pkg "$1")}")
}

_jd_cls() {
    reply=("${(@f)$(_jd_list_cls "$1")}")
}

compctl -q -S '.' -M 'm:{a-zA-Z}={A-Za-z}' -K _jd_pkg -t+ + \
        -M 'm:{a-zA-Z}={A-Za-z}' -K _jd_cls jd
