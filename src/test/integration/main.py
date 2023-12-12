import sys

from visualization_tests import perform_visualization_tests

if __name__ == '__main__':
    if len(sys.argv) != 6:
        raise Exception("Usage: [script] [JAVA_HOME] [UAV_visualization jar file directory] "
                        "[UAV_visualization jar file name] [assets checksum] [UAVStateGenerator class module]")
    perform_visualization_tests(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])
