########################
Contributing to Pymaging
########################


*********
Community
*********

People interested in developing for the Pymaging should join the #pymaging
IRC channel on `freenode`_ for help and to discuss the development.


*****************
Contributing Code
*****************


General
=======

- Code **must** be tested. Untested patches will be declined.
- If a patch affects the public facing API, it must document these changes.
- If a patch changes code, the internal documentation (docs/dev/) must be updated to reflect the changes.

Since we're hosted on GitHub, pymaging uses `git`_ as a version control system.

If you're not familiar with git, check out the `GitHub help`_ page.


Syntax and conventions
======================

We try to conform to `PEP8`_ as much as possible. This means 4 space
indentation.


Process
=======

This is how you fix a bug or add a feature:

#. `fork`_ us on GitHub.
#. Checkout your fork.
#. Hack hack hack, test test test, commit commit commit, test again.
#. Push to your fork.
#. Open a pull request.


Tests
=====

If you're unsure how to write tests, feel free to ask for help on IRC.

Running the tests
-----------------

To run the tests we recommend using ``nose``. If you have ``nose`` installed,
just run ``nosetests`` in the root directory. If you don't, you can also use
``python -m unittest discover``.


**************************
Contributing Documentation
**************************

The documentation is written using `Sphinx`_/`restructuredText`_. 

Section style
=============

We use Python documentation conventions fo section marking:

* ``#`` with overline, for parts
* ``*`` with overline, for chapters
* ``=``, for sections
* ``-``, for subsections
* ``^``, for subsubsections
* ``"``, for paragraphs


.. _fork: http://github.com/ojii/pymaging
.. _Sphinx: http://sphinx.pocoo.org/
.. _PEP8: http://www.python.org/dev/peps/pep-0008/
.. _GitHub : http://www.github.com
.. _GitHub help : http://help.github.com
.. _freenode : http://freenode.net/
.. _pull request : http://help.github.com/send-pull-requests/
.. _git : http://git-scm.com/
.. _restructuredText: http://docutils.sourceforge.net/docs/ref/rst/introduction.html

