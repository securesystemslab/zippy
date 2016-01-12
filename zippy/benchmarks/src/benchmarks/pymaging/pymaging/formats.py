# -*- coding: utf-8 -*-
# Copyright (c) 2012, Jonas Obrist
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#    * Redistributions of source code must retain the above copyright
#      notice, this list of conditions and the following disclaimer.
#    * Redistributions in binary form must reproduce the above copyright
#      notice, this list of conditions and the following disclaimer in the
#      documentation and/or other materials provided with the distribution.
#    * Neither the name of the Jonas Obrist nor the
#      names of its contributors may be used to endorse or promote products
#      derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL JONAS OBRIST BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
from collections import namedtuple
import threading

Format = namedtuple('Format', 'open save extensions')



class FormatRegistry(object):
    # Use the Borg pattern to share state between all instances. Details at
    # http://aspn.activestate.com/ASPN/Cookbook/Python/Recipe/66531.
    __shared_state = dict(
        names = {},
        formats = [],

        # -- Everything below here is only used when populating the registry --
        loaded = False,
        write_lock = threading.RLock(),
    )

    def __init__(self):
        self.__dict__ = self.__shared_state
    
    def _populate(self):
        if self.loaded:
            return
        with self.write_lock:
            import pkg_resources
            for entry_point in pkg_resources.iter_entry_points('pymaging.formats'):
                format = entry_point.load()
                self.register(format)
            self.loaded = True
    
    def register(self, format):
        self.formats.append(format)
        for extension in format.extensions:
            self.names[extension] = format
        
    def get_format_objects(self):
        self._populate()
        return self.formats
    
    def get_format(self, format):
        self._populate()
        return self.names.get(format, None)

registry = FormatRegistry()
get_format_objects = registry.get_format_objects
get_format = registry.get_format
register = registry.register
