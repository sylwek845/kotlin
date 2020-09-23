// NOTE: FIR infers `COLUMN_TITLES?.size` to have type `Int` producing a `(null as Number).intValue()`
//       in the `COLUMN_TITLES == null` case (which is never executed).

public class StockMarketTableModel() {

    public fun getColumnCount() : Int {
        return COLUMN_TITLES?.size!!
    }

    companion object {
        private val COLUMN_TITLES : Array<Int?> = arrayOfNulls<Int>(10)
    }
}

fun box() : String = if(StockMarketTableModel().getColumnCount()==10) "OK" else "fail"
