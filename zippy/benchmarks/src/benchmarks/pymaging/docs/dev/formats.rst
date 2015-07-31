###############
Writing formats
###############

Formats in pymaging are represented as :class:`pymaging.formats.Format` instances. To make your own format, create an
instance of that class, giving a method to **decode**, a method to **encode** and a list of **extensions** for this
format as arguments.

*******
Decoder
*******

The decoder function takes one file-like object as argument. It should return ``None`` if the file object passed in is
not in the format handled by this decoder, otherwise it should return an instance of :class:`pymaging.image.Image`. For
help with image objects, see :ref:`about-image-objects`.

*******
Encoder
*******

The encoder takes an instance of :class:`pymaging.image.Image` and a file-like object as arguments and should save the
image to that file object. For help with image objects, see :ref:`about-image-objects`.
