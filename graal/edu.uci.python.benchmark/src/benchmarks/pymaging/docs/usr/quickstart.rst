##########
Quickstart
##########


*****************
Resizing an image
*****************

Resizing ``myimage.png`` to 300x300 pixels and save it as ``resized.png``::

    from pymaging import Image
    
    img = Image.open_from_path('myimage.png')
    img = img.resize(300, 300)
    img.save('resized.png')
