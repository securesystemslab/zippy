# -*- coding: utf-8 -*-
from pymaging import __version__
from setuptools import setup

setup(
    name = "pymaging",
    version = __version__,
    packages = ['pymaging'],
    author = "Jonas Obrist",
    author_email = "ojiidotch@gmail.com",
    description = "Pure Python imaging library.",
    license = "BSD",
    keywords = "pymaging png imaging",
    url = "https://github.com/ojii/pymaging/",
    zip_safe = False,
    test_suite = 'pymaging.tests',
)
