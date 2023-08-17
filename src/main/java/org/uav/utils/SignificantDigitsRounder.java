package org.uav.utils;

public class SignificantDigitsRounder {
    // https://stackoverflow.com/questions/7548841/round-a-double-to-3-significant-figures

    public static double sigDigRounder(double value, int nSigDig, int dir) {

        double pow = Math.pow(10, Math.floor(Math.log10(Math.abs(value))) - (nSigDig - 1));

        double intermediate = value/ pow;

        if(Double.isNaN(intermediate))
            return 0;

        if(dir > 0)      intermediate = Math.ceil(intermediate);
        else if (dir< 0) intermediate = Math.floor(intermediate);
        else             intermediate = Math.round(intermediate);

        return(intermediate * pow);

    }
}
