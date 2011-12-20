import sqlite3

class BenchmarkDb:
    def __init__(self, path):
        self.con = sqlite3.connect(path)
        c = self.con.cursor();
        c.execute('pragma foreign_keys = on')
        c.execute('create table if not exists results (revnum integer, foreign key(benchmarkid) references benchmarks(benchmarkid), foreign key(valueid) references benchmarkvalues(valueid), value real)')
        c.execute('create table if not exists benchmarks (benchmarkid integer primary key autoincrement, name text)')
        c.execute('create table if not exists benchmarkvalues (valueid integer primary key autoincrement, name text)')
        self.con.commit()
        c.close()

    def insertResults(self, revision, results):
        c = self.con.cursor()
        for result in results:
            if not result.has_key('benchmark'):
                continue
            benchmarkName = result['benchmark']
            del result['benchmark']
            if len(result.keys()) <= 0:
                continue
            
            benchIdRow = c.execute('select benchmarkid from benchmarks where name="' + benchmarkName + '"').fetchone()
            benchId = -1;
            if benchIdRow is None:
                c.execute('insert into benchmarks (name) values ("' + benchmarkName + '")  values ')
                benchId = c.lastrowid()
            else:
                benchId = benchIdRow['benchmarkid']
            
            insertcmd = 'insert into results (revnum, benchmarkid, valueid, value) '
            first = True
            for valueName in result.keys():
                valueIdRow = c.execute('select valueid from benchmarkvalues where name="' + valueName + '"').fetchone()
                valueId = -1;
                if valueIdRow is None:
                    c.execute('insert into benchmarkvalues (name) values ("' + valueName + '")')
                    valueId = c.lastrowid()
                else:
                    valueId = benchIdRow['valueid']
                
                if first:
                    insertcmd = insertcmd + 'select ' + str(revision) + ' as revnum, ' + str(benchId) + ' as benchmarkid, ' + str(valueId) + ' as valueid, ' + result[valueName] + ' as value '
                else:
                    insertcmd = insertcmd + 'union select ' + str(revision) + ', ' + str(benchId) + ', ' + str(valueId) + ', ' + result[valueName] + ' '
            c.execute(insertcmd)
        self.con.commit()
        c.close()
        
    def close(self):
        self.con.close()