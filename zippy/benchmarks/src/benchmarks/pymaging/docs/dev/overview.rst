########
Overview
########


.. _about-image-objects:

*******************
About Image objects
*******************


:class:`pymaging.image.Image` vs :class:`pymaging.image.LoadedImage`
====================================================================

There are two main classes for representing an image in pymaging, :class:`pymaging.image.Image` and
:class:`pymaging.image.LoadedImage`. Other than their constructor their APIs are the same. The difference is that
:class:`pymaging.image.Image` didn't load all the image data from the file yet, whereas
:class:`pymaging.image.LoadedImage` did. As a general rule, any format should use :class:`pymaging.image.Image` so
opening an image will first load it's metadata (width, height) before loading all the pixel data (which can consume
large amounts of memory). This is useful for users who just want to verify that what they have is an image supported by
pymaging and maybe want to know the dimensions of the image before loading it.

:class:`pymaging.image.LoadedImage` should only be used if you have all the pixel data in memory anyway or if there's no
way around loading all the data at first, as it's required to extract the meta information.


About loaders
=============

:class:`pymaging.image.Image` takes a loader callable which will be called to actually load the image data. This loader
should return a tuple ``(pixel_array, palette)``. ``pixel_array` should be constructed with
:func:`pymaging.pixelarray.get_pixel_array`, whereas ``palette`` should either be a palette (list of colors) or
``None``.


************
Pixel arrays
************

Pixel arrays are the core data structure in which image data is represented in pymaging. Their base class is
:class:`pymaging.pixelarray.GenericPixelArray`, but in practice they use one of the specialized subclasses. In almost
all cases, you should use :func:`pymaging.pixelarray.get_pixel_array` to construct pixel arrays, instead of using the
classes directly.

:func:`pymaging.pixelarray.get_pixel_array` takes the image **data** (as an :class:`array.array`, more on this later),
the **width** (in pixels), **height** (in pixels) and the **pixel size** as arguments and returns a, if possible
specialized, pixel array.


Pixel size
==========

The **pixel size** indicates how many bytes form a single pixel. It also describes how data is stored in the array
passed into the pixel array. A pixel size of one indicates either an image with a palette (where the bytes in the image
data are indices into the palette) or a monochrome image. Pixel size 3 is probably the most common and usually indicates
RGB, whereas pixel size 4 indicates RGBA.

Given the **pixel size**, the **data** passed into the pixel array is translated into pixels at x/y coordinates through
the APIs on pixel array.

.. module:: pymaging.pixelarray

Important methods
=================

You should hardly ever manipulate the ``data`` attribute on pixel arrays directly, instead, you should use the provided
APIs that handle things like x/y translation for the given width, height and pixel size.

Pixel array methods usually operate **in place**, if you wish to have a copy of the data, use ``copy()``.

``get(x, y)``
-------------

Returns the **pixel** (a list of ints) at the given position.

``set(x, y, pixel)``
--------------------

Sets the given pixel (list of ints) to the given position.

``remove_lines``, ``remove_columns``, ``add_lines`` and ``add_columns``
-----------------------------------------------------------------------

Those four methods are closely related and are used to resize a pixel array (and thus the image canvas). They all take
two arguments: ``amount`` and ``offset``.

.. warning::

    There is an important performance caveat with those four methods. Manipulating columns (``add_columns`` and
    ``remove_columns``) is slower the more lines there are. Therefore the column manipulating methods should always be
    called **before add_lines** or **after remove_lines** to keep the amount of lines where columns are changed the
    lowest.
