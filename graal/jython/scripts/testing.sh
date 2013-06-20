NOW=$(date +%m%d%y_%H%M)
HELP=0
DO_CPYTHON=1
DO_ALL=1
USE_ARGUMENT=0
ARGUMENT=5
# CPython=~/jython2.7b1/./jython
CPython=python3.3
TESTRESUTLFOLDER="python_$NOW"
OURTESTRESUTLFOLDER="ourJython_$NOW"


while true; do
  case "$1" in
    -h|--help) HELP=1 ; break ;;
    -s|--selected) DO_ALL=0 ; FILE_TO_EXEC="$2" ; break ;;
    -j|--justours)  DO_CPYTHON=0 ;  ARGUMENT=$2 ; FOLDER_TO_CMP="$3" ; break ;;
    -a|--argument)  USE_ARGUMENT=1 ; ARGUMENT=$2 ; break ;;
    --)        shift ; break ;;
    *)         break ;;
  esac
  shift
done

echo "Computing using argument: $ARGUMENT" ; 



if [ $HELP == 1 ]; 
	then
  	echo "****All test files must be inside 'benchmarks' folder****"
  	echo "    -h | --help"
  	echo "    -s | --selected [filename.py] [argument]"
  	echo "    -j | --justours [argument] [existing test results folder]"
  	echo "    -a | --argument"
  	exit 1
fi

if [ $DO_ALL == 0 ]; 
	then
	ARGUMENT=$3
	echo "Computing $2 with python..."
	
	$CPython "benchmarks/$2" $ARGUMENT
	
	echo "Computing $2 with Our Jython..."
	
	./jython.py -x "$2" $ARGUMENT -O "-interpretast -specialize"

  	exit 1
fi

mkdir $TESTRESUTLFOLDER
echo "Create $TESTRESUTLFOLDER folder for test results"

mkdir $OURTESTRESUTLFOLDER
echo "Create $OURTESTRESUTLFOLDER folder for test results"

cd benchmarks
for file in *.py;
	do
	$CPython "$file" $ARGUMENT >> ../$TESTRESUTLFOLDER/"$file".txt 2> ../$TESTRESUTLFOLDER/"$file"_error.log
	cd ..
	./jython.py -x "$file" $ARGUMENT -O "-interpretast -specialize" >> $OURTESTRESUTLFOLDER/"$file".txt 2> $OURTESTRESUTLFOLDER/"$file"_error.log

	RESULT=$(python doFileCMP.py "$OURTESTRESUTLFOLDER/$file.txt" "$TESTRESUTLFOLDER/$file.txt")
	PASS_OR_FAIL="\e[31m FAIL \033[0m"
	NOTE="Please See $OURTESTRESUTLFOLDER/$file.txt and "$file"_error.log for details"

	if [ $RESULT == "True" ]; 
	then 
		PASS_OR_FAIL="\e[32m PASS \033[0m"
		NOTE=""
	fi
	printf '%-40s' $file && printf "[$PASS_OR_FAIL] $NOTE\n"

	cd benchmarks
	done
cd ..

# python doCMP.py "our_test_results_$NOW" "original_test_results_$NOW"
