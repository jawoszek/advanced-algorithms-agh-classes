import java.util.concurrent.Callable
import java.util.concurrent.FutureTask

class MatricesMultiplication implements Callable<Matrix> {

    List<Matrix> matrices

    static void main(String[] args) {
        long millisBeforeParsing = System.currentTimeMillis()
        List<Matrix> matrices = readFromFile('src/main/resources/matricesFromClass4.txt')
        long millisAfterParsing = System.currentTimeMillis()
        println "time of parsing: ${millisAfterParsing-millisBeforeParsing} ms"

        long millisBeforeCalculation = System.currentTimeMillis()
        Matrix result = new MatricesMultiplication(matrices: matrices).call()
        long millisAfterCalculation = System.currentTimeMillis()
        println result
        println "time of calculation: ${millisAfterCalculation-millisBeforeCalculation} ms"
    }

    private static List<Matrix> readFromFile(String filePath) {
        List<Matrix> matrices = []
        Matrix currentMatrix = null
        int currentRow = 0
        int currentColumn = 0


        String text = new File(filePath).getText()
        def matrixHeaderPattern = /Matrix #  \d+ \[ (\d+)  x  (\d+) ]/
        def valuePattern = /(?:\d+\.\d+e?-?\d*)/
        for (String line : text.readLines()) {
            if (!currentMatrix) {
                def matcher = line =~ matrixHeaderPattern

                if (!matcher) {
                    continue
                }

                currentRow = 0
                currentColumn = 0
                currentMatrix = new Matrix(Integer.parseInt(matcher[0][1]), Integer.parseInt(matcher[0][2]))
            }

            def matcher = line =~ valuePattern

            if (!matcher) {
                continue
            }

            matcher.each {
                double value = Double.parseDouble(it)
                currentMatrix.rows[currentRow][currentColumn] = value
                currentMatrix.columns[currentColumn][currentRow] = value
                currentColumn++
            }

            int numberOfClosings = line.count(']')

            if (numberOfClosings > 0) {
                currentRow++
                currentColumn = 0
            }

            if (numberOfClosings > 1) {
                matrices.add(currentMatrix)
                currentMatrix = null
            }
        }
        matrices
    }

    @Override
    Matrix call() throws Exception {
        if (matrices.size() < 4) {
            return multiplyMatrices(matrices)
        }

        List<Matrix> toMultiply = matrices.subList(0, matrices.size().intdiv(2))
        List<Matrix> toSend = matrices.subList(matrices.size().intdiv(2), matrices.size())

        Callable<Matrix> callable = new MatricesMultiplication(matrices: toSend)
        FutureTask<Matrix> futureTask = new FutureTask<>(callable)
        new Thread(futureTask).start()

        Matrix calculated = multiplyMatrices(toMultiply)
        Matrix received = futureTask.get()

        calculated * received
    }

    private static Matrix multiplyMatrices(List<Matrix> matrices) {
        matrices.inject { result, matrix -> result * matrix }
    }

    private static class Matrix {
        double[][] rows
        double[][] columns

        Matrix() {

        }

        Matrix(int rowsSize, int columnsSize) {
            rows = new double[rowsSize][]
            columns = new double[columnsSize][]
            for (int i = 0; i < rowsSize; i++) {
                rows[i] = new double[columnsSize]
            }
            for (int i = 0; i < columnsSize; i++) {
                columns[i] = new double[rowsSize]
            }
        }

        Double[] row(int index) {
            rows[index]
        }

        Double[] column(int index) {
            columns[index]
        }

        Matrix multiply(Matrix other) {
            if (this.columns.length != other.rows.length) {
                throw new IllegalArgumentException("Matrices sizes [${this.rows.length} x ${this.columns.length}] and [${other.rows.length} x ${other.columns.length}] are not compatible")
            }

            double[][] rows = new double[this.rows.length][]
            double[][] columns = new double[other.columns.length][]
            for (int i = 0; i < this.rows.length; i++) {
                rows[i] = new double[other.columns.length]
            }
            for (int i = 0; i < other.columns.length; i++) {
                columns[i] = new double[this.rows.length]
            }

            for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
                for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
                    double value = [this.rows[rowIndex], other.columns[columnIndex]].transpose().collect {
                        it[0] * it[1]
                    }.sum()
                    rows[rowIndex][columnIndex] = value
                    columns[columnIndex][rowIndex] = value
                }
            }

            new Matrix(rows: rows, columns: columns)
        }

        @Override
        String toString() {
            return "[${Arrays.stream(rows).collect { row -> "[${Arrays.stream(row).collect { value -> Double.toString(value) }.join(' ')}]" }.join('\n')}]"
        }
    }
}
