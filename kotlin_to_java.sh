#!/usr/bin/env bash

set -xeu


perl -i -p -e 's/fun\s*([\w<>:, ]+)\(([\w:<>*?,= ]*)\):\s*([\w:<>*?, ]+)\s*{/$3 $1($2) {/g' $1
perl -i -p -e 's/fun\s*([\w<>:, ]+)\(([\w:<>*?,= ]*)\)\s*{/void $1($2) {/g' $1

perl -i -p -e 's/fun\s*([\w<>:, ]+)\(([\w:<>*?,= ]*)\):\s*([\w:<>*?, ]+)/$3 $1($2);/g' $1
perl -i -p -e 's/fun\s*([\w<>:, ]+)\(([\w:<>*?,= ]*)\)/void $1($2);/g' $1

perl -i -p -e 's/([a-z][\w]*)\s*:\s*([\w:<>*? ]+)/$2 $1/g' $1

perl -i -p -e 's/([\w]+)\?/\@Nullable $1/g' $1

perl -i -p -e 's/(\b)In(\b)/$1Serializable$2/g' $1
perl -i -p -e 's/(\b)Key(\b)/$1Serializable$2/g' $1
perl -i -p -e 's/(\b)Out(\b)/$1\@Nullable Serializable$2/g' $1

perl -i -p -e 's/(\b)Any(\b)/$1Object$2/g' $1
perl -i -p -e 's/(\b)Boolean(\b)/$1boolean$2/g' $1
perl -i -p -e 's/(\b)Int(\b)/$1int$2/g' $1
perl -i -p -e 's/(\b)Long(\b)/$1long$2/g' $1

perl -i -p -e 's/(\b)val(\b)/$1final$2/g' $1
perl -i -p -e 's/(\b)var(\b)/$1$2/g' $1
perl -i -p -e 's/(\b)override(\b)/$1\@Override public$2/g' $1
perl -i -p -e 's/(\b)in(\b)/$1:$2/g' $1
perl -i -p -e 's/(\b)out(\b)//g' $1
perl -i -p -e 's/(\b)javaClass(\b)/$1getClass()$2/g' $1

perl -i -p -e 's/\@Volatile/volatile/g' $1
