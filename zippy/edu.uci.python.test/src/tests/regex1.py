import re
ESCAPE = re.compile(u'[\\x00-\\x1f\\\\"\\b\\f\\n\\r\\t\u2028\u2029]')
