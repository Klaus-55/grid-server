'reinit'
'set parea 0.6 10.7 0.8 8' 
'set grads off'   
'set mpdraw off'  
'set datawarn off'  
'set grid on'  
'set frame off'
'set map 50 1 1'
'set mproj scaled'    

'set font 24 file #(fontpath??"C:/opengrads/")arial.ttf' 
'set xlopts 1 4 0.13'
'set ylopts 1 4 0.13'

'set rgb 27 255 255 255 '
'set rgb 20 255 255 255 '
'set rgb 21 186 244 168 '
'set rgb 22 107 203 109 '
'set rgb 23 133 200 252 '
'set rgb 24 60 62 255 '
'set rgb 25 249 62 251 '
'set rgb 26 160 69 100 '
 
'sdfopen #(ncFile)'
'set lon 112.8 115.5'
'set lat 21.5 23.5'

'set cmax -1000'
'set t 1'
'd r03h'

'set gxout shaded'
'set clevs 1 3 10 20 50 70'  
'set ccols  27  21 22 23 24 25 26'
'set csmooth on'
'set t #(time)'
'd smth9(#(valuename))'
'cbar_matlab 1 1 0 5.5 0.3' 

'define maxa = max(#(valuename),lat=21.5,lat=23.5) '
'define maxb = max(#(valuename),lon=112.8,lon=115.5) '

'define maxv = max( max(#(valuename),lat=21.5,lat=23.5),lon=112.8,lon=115.5)'
'd maxv'
maxvalue=subwrd(result,4)

if(maxvalue>0)
'define locx=maxloc(maxa,lon=112.8,lon=115.5)'
'define locy=maxloc(maxb,lat=21.5,lat=23.5)'
'd locx'
locxx=subwrd(result,4)
'd locy'
locyy=subwrd(result,4)
'q gr2w 'locxx' 'locyy
x1=subwrd(result,3)
y1=subwrd(result,6)
'q w2xy 'x1' 'y1''
x=subwrd(result,3)
y=subwrd(result,6)
'set line 0'
'draw mark 3 'x' 'y' 0.14'
'set line 2'
'draw mark 3 'x' 'y' 0.12'

*'set string 1 c 8'
*'draw string 'x' 'y+0.2' 'maxvalue

'draw string 10 0.46 Max:'math_format('%3.1f',maxvalue)
'set line 0'
'draw mark 3 9.3 0.38 0.14'
'set line 2'
'draw mark 3 9.3 0.38 0.12'
endif


'set line 1 1 3' 
'drawframe_diy'

'set rgb 30 160 160 160 200'
'set line 30 1 1' 
'draw shp #(mappath??"C:/opengrads/map/")gd.shp'  
'draw shp #(mappath??"C:/opengrads/map/")province.shp' 

'set rgb 31 133 133 133 200'
'set line 31 1 4' 
'draw shp #(mappath??"C:/opengrads/map/")shenzhen.shp'  
'draw shp #(mappath??"C:/opengrads/map/")country.shp'  
'set rgb 32 80 80 80 200' 
'set line 32 1 4' 
'draw shp #(mappath??"C:/opengrads/map/")coastline.shp' 


'gxprint'
'printim #(outFile) png x1024 white'
'disable gxprint'


