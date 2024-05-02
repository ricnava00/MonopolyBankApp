#!/bin/bash
rm -rf final;
mkdir final;
for f in {1..7}
do
{
	convert base/back.png -modulate 100,100,$((200/7*$f-12)) base/front.png -composite edit_$f.png;
	convert -background none -font URW-Gothic-L-Demi -size 500x500 label:$f out.png
	convert -trim out.png test.png
	convert -resize 250x250 test.png out.png
	convert -size 335x335 xc:none out.png -gravity center -composite rect.png
	convert base/circle.png rect.png -compose DstOut -composite num_${f}.png
	convert edit_${f}.png num_${f}.png -geometry +2070+1170 -composite final/${f}.png
}
done;
rm -rf edit_*;
rm rect.png;
rm test.png;
rm out.png;
rm -rf num_*;