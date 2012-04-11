#!/bin/bash
 #
 echo ---Bg---40---41---42---43---44---45---46---47
 for i in {30..37} # foreground
 do
 echo -n -e fg$i- 
 for j in {40..47} # background
 do
 echo -n -e '\E['$i';'$j'm SS64'
 tput sgr0 # Reset text attributes to normal without clear
 done
 echo # newline
 done
 
 echo -- Clear BG --
 for n in {30..37} # foreground
 do
 echo -e fg$n '\E['$n';'01'm SS64'
 tput sgr0 # Reset text attributes to normal without clear
 done