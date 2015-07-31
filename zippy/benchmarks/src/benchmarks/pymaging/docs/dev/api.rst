############
Internal API
############


.. module:: pymaging.image

*********************
:mod:`pymaging.image`
*********************


.. class:: Image(mode, width, height, loader, meta=None)

    The image class. This is the core class of pymaging.

    :param mode: The color mode. A :class:`pymaging.colors.ColorType` instance.
    :param width: Width of the image (in pixels).
    :param height: Height of the image (in pixels).
    :param loader: A callable which when called returns a tuple containing a
                   :class:`pymaging.pixelarray.GenericPixelArray` instance and a palette (which can be ``None``). If you
                   already have all the pixels for your image loaded, use :class:`pymaging.image.LoadedImage` instead.
    :param meta: Any further information your format wants to pass along. Your format should document what users can
                 expect in ``meta``.

    .. attribute:: mode

        The color mode used in this image. A :class:`pymaging.colors.ColorType` instance.

    .. attribute:: palette

        The palette used in this image. A list of :class:`pymaging.colors.Color` instances or ``None``.

    .. attribute:: reverse_palette

        Cache for :meth:`get_reverse_palette`.

    .. attribute:: pixels

        The :class:`pymaging.pixelarray.GenericPixelArray` (or subclass thereof) instance holding the image data.

    .. attribute:: width

        The width of the image (in pixels)

    .. attribute:: height

        The height of the image (in pixels)

    .. attribute:: pixelsize

        The size of a pixel (in :attr:`pixels`). ``1`` usually indicates an image with a palette. ``3`` is an standard
        RGB image. ``4`` is a RGBA image.

    .. classmethod:: open(fileobj)

        Creates a new image from a file object

        :param fileobj: A file like object open for reading.

    .. classmethod:: open_from_path(filepath)

        Creates a new image from a file path

        :param fileobj: A string pointing at a image file.

    .. classmethod:: new(mode, width, height, background_color, palette=None, meta=None)

        Creates a new image with a solid background color.

        :param mode: The color mode. Must be an instance of :class:`pymaging.colors.ColorType`.
        :param width: Width of the new image.
        :param height: Height of the new image.
        :param background_color: The color to use for the background. Must be an instance of
                                 :class:`pymaging.colors.Color`.
        :param palette: If given, the palette to use for the image.
        :param meta: Any further information your format wants to pass along. Your format should document what users can
                     expect in ``meta``.

    .. method:: save(fileobj, format)

        Saves the image.

        :param fileobj: A file-like object (opened for writing) to which the image should be saved.
        :param format: The format to use for saving (as a string).

    .. method:: save_to_path(filepath, format=None):

        Saves the image to a path.

        :param filepath: A string pointing at a (writable) file location where the image should be saved.
        :param format: If given, the format (string) to use for saving. If ``None``, the format will be guessed from
                       the file extension used in ``filepath``.

    .. method:: get_reverse_palette

        Returns :attr:`reverse_palette`. If :attr:`reverse_palette` is ``None``, calls :meth:`_fill_reverse_palette`.
        The reverse palette is a dictionary. If the image has no palette, an empty dictionary is returned.

    .. method:: _fill_reverse_palette

        Populates the reverse palette, which is a mapping of :class:`pymaging.colors.Color` instances to their index in
        the palette. Sets :attr:`reverse_palette`.

    .. method:: _copy(pixles, **kwargs)

        Creates a copy of this instances meta information, but setting pixel array to ``pixels``. ``kwargs`` can
        override any argument to the :class:`pymaging.image.LoadedImage` constructor. By default the values of this
        image are used.

        This method is mostly used by other APIs that return a new copy of the image.

        Returns a :class:`pymaging.image.LoadedImage`.

    .. method:: resize(width, height, resample_algorithm=nearest, resize_canvas=True)

        Resizes the image to the given ``width`` and ``height``, using given ``resample_algorithm``. If
        ``resize_canvas`` is ``False``, the actual image dimensions do not change, in which case the excess pixels will
        be filled by a background color (usually black). Returns the resized copy of this image.

        :param width: The new width as integer in pixels.
        :param height: The new height as integer in pixels.
        :param resample_algorithm: The resample algorithm to use. Should be a :class:`pymaging.resample.Resampler`
                                   instance.
        :param resize_canvas: Boolean flag whether to resize the canvas or not.

    .. method:: affine(transform, resample_algorithm=nearest, resize_canvas=True)

        Advanced version of :meth:`resize`. Instead of a ``height`` and ``width``, a
        :class:`pymaging.affine.AffineTransform` is passed according to which the image is transformed.
        Returns the transformed copy of the image.

    .. method:: rotate(degrees, clockwise=False, resample_algorithm=nearest, resize_canvas=True)

        Rotates the image by ``degrees`` degrees counter-clockwise (unless ``clockwise`` is ``True``). Interpolation of
        the pixels is done using ``resample_algorithm``. Returns the rotated copy of this image.

    .. method:: get_pixel(x, y)

        Returns the pixel at the given ``x``/``y`` location. If the pixel is outside the image, raises an
        :exc:`IndexError`. If the image has a palette, the palette lookup will be performed by this method. The pixel is
        returned as a list if integers.

    .. method:: get_color(x, y)

        Same as :meth:`get_pixel` but returns a :class:`pymaging.colors.Color` instance.

    .. method:: set_color(x, y, color)

        The core drawing API. This should be used to draw pixels to the image. Sets the pixel at ``x``/``y`` to the
        color given. The color should be a :class:`pymaging.colors.Color` instance. If the image has a palette, only
        colors that are in the palette are supported.

    .. method:: flip_top_bottom

        Vertically flips the image and returns the flipped copy.

    .. method:: flip_left_right

        Horizontally flips the image and returns the flipped copy.

    .. method:: crop(width, height, padding_top, padding_left)

        Crops the pixel to the new ``width`` and ``height``, starting the cropping at the offset given with
        ``padding_top`` and ``padding_left``. Returns the cropped copy of this image.

    .. method:: draw(shape, color)

        Draws the shape using the given color to this image. The shape should be a :class:`pymaging.shapes.BaseShape`
        subclass instance, or any object that has a ``iter_pixels`` method, which when called with a
        :class:`pymaging.colors.Color` instance, returns an iterator that yields tuples of ``(x, y, color)`` of colors
        to be drawn to pixels.

        This method is just a shortcut around :meth:`set_color` which allows users to write shape classes that do the
        heavy lifting for them.

        This method operates **in place** and does not return a copy of this image!

    .. method:: blit(padding_top, padding_left, image):

        Draws the image passed in on top of this image at the location indicated with the padding.

        This method operates **in place** and does not return a copy of this image!


.. class:: LoadedImage(mode, width, height, pixels, palette=None, meta=None)

    Subclass of :class:`pymaging.image.Image` if you already have all pixels loaded. All parameters are the same as in
    :class:`pymaging.image.Image` except for ``loader`` which is replaced with ``pixels``. ``pixels`` must be an
    instance of :class:`pymaging.pixelarray.GenericPixelArray` or a subclass thereof.


.. module:: pymaging.affine

**********************
:mod:`pymaging.affine`
**********************


.. class:: AffineTransform(matrix)

    Affine transformation matrix. Used by :meth:`pymaging.image.Image.affine`.

    The matrix should be given either as a sequence of 9 values or a sequence of 3 sequences of 3 values.

    .. note:: Needs documentation about the actual values of the matrix.

    .. attribute:: matrix

        .. note:: Needs documentation.

    .. method:: _determinant

        .. note:: Needs documentation.

    .. method:: inverse

        .. note:: Needs documentation.

    .. method:: rotate(degrees, clockwise=False)

        .. note:: Needs documentation.

    .. method:: scale(x_factor, y_factor=None)

        .. note:: Needs documentation.

    .. method:: translate(dx, dy)

        .. note:: Needs documentation.


.. module:: pymaging.colors

**********************
:mod:`pymaging.colors`
**********************

.. function:: _mixin_alpha(colors, alpha)

    Applies the given alpha value to all colors. Colors should be a list of three items: ``r``, ``g`` and ``b``.


.. class:: Color(red, green, blue alpha)

    Represents a color. All four parameters should be integers between 0 and 255.

    .. attribute:: red
    .. attribute:: green
    .. attribute:: blue
    .. attribute:: alpha

    .. classmethod:: from_pixel(pixel)

        Given a pixel (a list of colors), create a :class:`Color` instance.

    .. classmethod:: from_hexcode(hexcode)

        Given a hexcode (a string of 3, 4, 6 or 8 characters, optionally prefixed by ``'#'``), construct a
        :class:`Color` instance.

    .. method:: get_for_brightness(brightness)

        Given a brightness (alpha value) between 0 and 1, return the current color for that brightness.

    .. method:: cover_with(cover_color)

        Covers the current color with another color respecting their respective alpha values. If the ``cover_color``
        is a solid color, return a copy of the ``cover_color``. ``cover_color`` must be an instance of :class:`Color`.

    .. method:: to_pixel(pixelsize)

        Returns this color as a pixel (list of integers) for the given ``pixelsize`` (3 or 4).

    .. method:: to_hexcode

        Returns this color as RGBA hexcode. (Without leading ``'#'``).


.. class:: ColorType

    A named tuple holding the length of a color type (pixelsize) and whether this color type supports the alpha channel
    or not.

    .. attribute:: length
    .. attribute:: alpha


.. data:: RGB

    RGB :class:`ColorType`.

.. data:: RGBA

    RGBA :class:`ColorType`.


.. module:: pymaging.exceptions

*************************
:mod:`pymaging.exception`
*************************


.. exception:: PymagingExcpetion

    The root exception type for all exceptions defined in this module.

.. exception:: FormatNotSupported

    Raised if an image is saved or loaded in a format not supported by pymaging.

.. exception:: InvalidColor

    Raised if an invalid color is used on an image (usually when the image has a palette).


.. module:: pymaging.formats

***********************
:mod:`pymaging.formats`
***********************

Loads and maintains the formats supported in this installation.

.. class:: Format(open, save, extensions)

    A named tuple that should be used to define formats for pymaging. ``open`` and ``save`` are callables that
    decode and encode an image in this format. ``extensions`` is a list of file extensions this image type could have.

    .. attribute:: open
    .. attribute:: save
    .. attribute:: extensions

.. class:: FormatRegistry

    A singleton class for format registration

    .. method:: _populate

        Populates the registry using package resources.

    .. method:: register(format)

        Manually registers a format, which must be an instance of :class:`Format`.

    .. method:: get_format_objects

        Returns all formats in this registry.

    .. method:: get_format(format)

        Given a format name (eg file extension), returns the :class:`Format` instance if it's registered, otherwise
        ``None``.

.. data:: registry

    The singleton instance of :class:`FormatRegistry`.

.. function:: get_format_objects

    Shortcut to :data:`registry.get_format_objects`.

.. function:: get_format

    Shortcut to :data:`registry.get_format`.

.. function:: register

    Shortcut to :data:`registry.register`.


.. module:: pymaging.helpers

***********************
:mod:`pymaging.helpers`
***********************


.. function:: get_transformed_dimensions(transform, box)

    Takes an affine transform and a four-tuple of (x0, y0, x1, y1) coordinates. Transforms each corner of the given box,
    and returns the (width, height) of the transformed box.


.. module:: pymaging.pixelarray

**************************
:mod:`pymaging.pixelarray`
**************************


.. class:: GenericPixelArray(data, width, height, pixelsize)

    The base pixel array class. ``data`` should be a flat :class:`array.array` instance of pixel data, ``width`` and
    ``height`` are the dimensions of the array and ``pixelsize`` defines how many items in the ``data`` array define a
    single pixel.

    Use :func:`get_pixel_array` to instantiate this class!

    .. attribute:: data

        The image data as array.

    .. attribute:: width

        The width of the pixel array.

    .. attribute:: height

        The height of the pixel array.

    .. attribute:: pixelsize

        The size of a single pixel

    .. attribute:: line_length

        The length of a line. (:attr:`width` multiplied with :attr:`pixelsize`).

    .. attribute:: size

        The size of the pixel array.

    .. method:: _precalculate

        Precalculates :attr:`line_width` and :attr:`size`. Should be called whenever :attr:`width`, :attr:`height` or
        :attr:`pixelsize` change.

    .. method:: _translate(x, y)

        Translates the logical ``x``/``y`` coordinates into the start of the pixel in the pixel array.

    .. method:: get(x, y)

        Returns the pixel at ``x``/``y`` as list of integers.

    .. method:: set(x, y, pixel)

        Sets the ``pixel`` to ``x``/``y``.

    .. method:: copy_flipped_top_bottom

        Returns a copy of this pixel array with the lines flipped from top to bottom.

    .. method:: copy_flipped_left_right

        Returns a copy of this pixel array with the lines flipped from left to right.

    .. method:: copy

        Returns a copy of this pixel array.

    .. method:: remove_lines(offset, amount)

        Removes ``amount`` lines from this pixel array after ``offset`` (from the top).

    .. method:: remove_columns(offset, amount)

        Removes ``amount`` columns from this pixel array after ``offset`` (from the left).

        .. note::

            If :meth:`remove_columns` and :meth:`remove_lines` are used together, :meth:`remove_lines` should always be
            called first, as that method is a lot faster and :meth:`remove_columns` gets faster the fewer lines there
            are in a pixel array.

    .. method:: add_lines(offset, amount, fill=0)

        Adds ``amount`` lines to the pixel array after ``offset`` (from the top) and fills it with ``fill``.

    .. method:: add_columns(offset, amount, fill=0)

        Adds ``amount`` columns to the pixel array after ``offset`` (from the left) and fill it with ``fill``.

        .. note::

            As with :meth:`remove_columns`, the cost of this method grows with the amount of lines in the pixe array.
            If it is used together with :meth:`add_lines`, :meth:`add_columns` should be called first.


.. class:: PixelArray1(data, width, height)

    Subclass of :class:`GenericPixelArray`, optimized for pixelsize 1.

    Use :func:`get_pixel_array` to instantiate this class!

.. class:: PixelArray2(data, width, height)

    Subclass of :class:`GenericPixelArray`, optimized for pixelsize 2.

    Use :func:`get_pixel_array` to instantiate this class!


.. class:: PixelArray3(data, width, height)

    Subclass of :class:`GenericPixelArray`, optimized for pixelsize 3.

    Use :func:`get_pixel_array` to instantiate this class!


.. class:: PixelArray4(data, width, height)

    Subclass of :class:`GenericPixelArray`, optimized for pixelsize 4.

    Use :func:`get_pixel_array` to instantiate this class!


.. function:: get_pixel_array(data, width, height, pixelsize)

    Returns the most optimal pixel array class for the given pixelsize. Use this function instead of instantating the
    pixel array classes directly.


.. module:: pymaging.resample

************************
:mod:`pymaging.resample`
************************


.. class:: Resampler

    Base class for resampler algorithms. Should never be instantated directly.

    .. method:: affine(source, transform, resize_canvas=True)

        .. note:: Document.

    .. method:: resize(source, width, height, resize_canvas=True)

        .. note:: Document.


.. class:: Nearest

    Subclass of :class:`Resampler`. Implements the nearest neighbor resampling algorithm which is very fast but creates
    very ugly resampling artifacts.


.. class:: Bilinear

    Subclass of :class:`Resampler` implementing the bilinear resampling algorithm, which produces much nicer results at
    the cost of computation time.


.. data:: nearest

    Singleton instance of the :class:`Nearest` resampler.


.. data:: bilinear

    Singleton instance of the :class:`Bilinear` resampler.


.. module:: pymaging.shapes

**********************
:mod:`pymaging.shapes`
**********************


Shapes are the high level drawing API used by :meth:`pymaging.image.Image.draw`.


.. class:: BaseShape

    Dummy base class for shapes.

    .. method:: iter_pixels(color)

        In subclasses, this is the API used by :meth:`pymaging.image.Image.draw` to draw to an image. Should return an
        iterator that yields ``x``, ``y``, ``color`` tuples.


.. class:: Pixel(x, y)

    A simple single-pixel drawing object.


.. class:: Line(start_x, start_y, end_x, end_y)

    Simple line drawing algorithm using the Bresenham Line Algorithm. Draws non-anti-aliased lines, which is very fast
    but for lines that are not exactly horizontal or vertical, this produces rather ugly lines.


.. class:: AntiAliasedLine(start_x, start_y, end_x, end_y)

    Draws an anti-aliased line using Xiaolin Wu's line algorithm. This has a lot higher computation costs than
    :class:`Line` but produces much nicer results. When used on an image with a palette, this shape might cause errors.


.. module:: pymaging.test_utils

**************************
:mod:`pymaging.test_utils`
**************************


.. function:: image_factory(colors, alpha=True)

    Creates an image given a list of lists of :class:`pymaging.color.Color` instances. The ``alpha`` parameter defines
    the pixel size of the image.


.. class:: PymagingBaseTestCase

    .. method:: assertImage(image, colors, alpha=True)

        Checks that an image is the same as the dummy image given. ``colors`` and ``alpha`` are passed to
        :func:`image_factory` to create a comparison image.


.. module:: pymaging.utils

*********************
:mod:`pymaging.utils`
*********************


.. function:: fdiv(a, b)

    Does a float division of ``a`` and ``b`` regardless of their type and returns a float.


.. function:: get_test_file(testfile, fname)

    Returns the full path to a file for a given test.


.. module:: pymaging.webcolors

*************************
:mod:`pymaging.webcolors`
*************************


Defines constant :class:`pymaging.color.Color` instances for web colors.

.. data:: IndianRed
.. data:: LightCoral
.. data:: Salmon
.. data:: DarkSalmon
.. data:: LightSalmon
.. data:: Red
.. data:: Crimson
.. data:: FireBrick
.. data:: DarkRed
.. data:: Pink
.. data:: LightPink
.. data:: HotPink
.. data:: DeepPink
.. data:: MediumVioletRed
.. data:: PaleVioletRed
.. data:: LightSalmon
.. data:: Coral
.. data:: Tomato
.. data:: OrangeRed
.. data:: DarkOrange
.. data:: Orange
.. data:: Gold
.. data:: Yellow
.. data:: LightYellow
.. data:: LemonChiffon
.. data:: LightGoldenrodYellow
.. data:: PapayaWhip
.. data:: Moccasin
.. data:: PeachPuff
.. data:: PaleGoldenrod
.. data:: Khaki
.. data:: DarkKhaki
.. data:: Lavender
.. data:: Thistle
.. data:: Plum
.. data:: Violet
.. data:: Orchid
.. data:: Fuchsia
.. data:: Magenta
.. data:: MediumOrchid
.. data:: MediumPurple
.. data:: BlueViolet
.. data:: DarkViolet
.. data:: DarkOrchid
.. data:: DarkMagenta
.. data:: Purple
.. data:: Indigo
.. data:: DarkSlateBlue
.. data:: SlateBlue
.. data:: MediumSlateBlue
.. data:: GreenYellow
.. data:: Chartreuse
.. data:: LawnGreen
.. data:: Lime
.. data:: LimeGreen
.. data:: PaleGreen
.. data:: LightGreen
.. data:: MediumSpringGreen
.. data:: SpringGreen
.. data:: MediumSeaGreen
.. data:: SeaGreen
.. data:: ForestGreen
.. data:: Green
.. data:: DarkGreen
.. data:: YellowGreen
.. data:: OliveDrab
.. data:: Olive
.. data:: DarkOliveGreen
.. data:: MediumAquamarine
.. data:: DarkSeaGreen
.. data:: LightSeaGreen
.. data:: DarkCyan
.. data:: Teal
.. data:: Aqua
.. data:: Cyan
.. data:: LightCyan
.. data:: PaleTurquoise
.. data:: Aquamarine
.. data:: Turquoise
.. data:: MediumTurquoise
.. data:: DarkTurquoise
.. data:: CadetBlue
.. data:: SteelBlue
.. data:: LightSteelBlue
.. data:: PowderBlue
.. data:: LightBlue
.. data:: SkyBlue
.. data:: LightSkyBlue
.. data:: DeepSkyBlue
.. data:: DodgerBlue
.. data:: CornflowerBlue
.. data:: RoyalBlue
.. data:: Blue
.. data:: MediumBlue
.. data:: DarkBlue
.. data:: Navy
.. data:: MidnightBlue
.. data:: Cornsilk
.. data:: BlanchedAlmond
.. data:: Bisque
.. data:: NavajoWhite
.. data:: Wheat
.. data:: BurlyWood
.. data:: Tan
.. data:: RosyBrown
.. data:: SandyBrown
.. data:: Goldenrod
.. data:: DarkGoldenrod
.. data:: Peru
.. data:: Chocolate
.. data:: SaddleBrown
.. data:: Sienna
.. data:: Brown
.. data:: Maroon
.. data:: White
.. data:: Snow
.. data:: Honeydew
.. data:: MintCream
.. data:: Azure
.. data:: AliceBlue
.. data:: GhostWhite
.. data:: WhiteSmoke
.. data:: Seashell
.. data:: Beige
.. data:: OldLace
.. data:: FloralWhite
.. data:: Ivory
.. data:: AntiqueWhite
.. data:: Linen
.. data:: LavenderBlush
.. data:: MistyRose
.. data:: Gainsboro
.. data:: LightGrey
.. data:: Silver
.. data:: DarkGray
.. data:: Gray
.. data:: DimGray
.. data:: LightSlateGray
.. data:: SlateGray
.. data:: DarkSlateGray
.. data:: Black
