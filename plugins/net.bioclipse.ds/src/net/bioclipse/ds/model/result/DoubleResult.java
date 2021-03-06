package net.bioclipse.ds.model.result;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * 
 * @author ola
 *
 */
public class DoubleResult extends SimpleResult implements IDoubleResult {
    
    DecimalFormat formatter;
    
    private double value;

    public DoubleResult(String name, Double value, int classification) {
        super( name, classification );
        this.value=value;
        
        DecimalFormatSymbols sym=new DecimalFormatSymbols();
        sym.setDecimalSeparator( '.' );
        formatter = new DecimalFormat("0.000", sym);
    }
    
    @Override
    public String getName() {
    	if (Double.isNaN(getValue()))
            return super.getName() + ": NaN" ;
    	else
        return super.getName() + ": " + formatter.format( getValue() );
    }
    
    
    public double getValue() {
        return value;
    }
    
    public void setValue( double value ) {
        this.value = value;
    }

}
