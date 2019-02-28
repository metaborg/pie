#!/usr/bin/env bash

set -xeu

perl -i -p -e 's/fun <([^>]+)>/<$1> fun/g' $1
perl -i -p -e 's/I : In/I extends In/g' $1
perl -i -p -e 's/O : Out/O extends Out/g' $1

perl -i -p -e 's/class (.+) : (.+)/class $1 implements $2/g' $1

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
perl -i -p -e 's/(\b)Float(\b)/$1float$2/g' $1
perl -i -p -e 's/(\b)MutableMap(\b)/$1HashMap$2/g' $1
perl -i -p -e 's/(\b)mutableMapOf(\b)/$1new HashMap$2/g' $1
perl -i -p -e 's/(\b)MutableSet(\b)/$1HashSet$2/g' $1
perl -i -p -e 's/(\b)mutableSetOf(\b)/$new HashSet$2/g' $1
perl -i -p -e 's/(\b)MutableList(\b)/$1ArrayList$2/g' $1
perl -i -p -e 's/(\b)mutableListOf(\b)/$1new ArrayList$2/g' $1
perl -i -p -e 's/(\b)ByteArray(\b)/$1byte[]$2/g' $1

perl -i -p -e 's/(\b)val(\b)/$1final$2/g' $1
perl -i -p -e 's/(\b)var(\b)/$1$2/g' $1
perl -i -p -e 's/(\b)override(\b)/$1\@Override public$2/g' $1
perl -i -p -e 's/(\b)in(\b)/$1:$2/g' $1
perl -i -p -e 's/(\b)out(\b)/$1$2/g' $1
perl -i -p -e 's/(\b)open(\b)/$1$2/g' $1
perl -i -p -e 's/(\b)javaClass(\b)/$1getClass()$2/g' $1
perl -i -p -e 's/(\b)run \{/$1{/g' $1

perl -i -p -e 's/\*/?/g' $1
perl -i -p -e 's/===/==/g' $1

perl -i -p -e 's/\@Volatile/volatile/g' $1
