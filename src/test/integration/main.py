import sys

from visualization_tests import perform_visualization_tests

if __name__ == '__main__':
    if len(sys.argv) != 5:
        raise Exception("Usage: [script] [JAVA_HOME] [UAV_visualization jar file directory] "
                        "[UAV_visualization jar file name] [assets checksum]")
    perform_visualization_tests(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
