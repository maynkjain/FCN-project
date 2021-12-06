package wsn;

import javafx.animation.Interpolator;

import static java.lang.Math.PI;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

public class SpringInterpolator extends Interpolator {
    @Override
    protected double curve(double x) {
        double factor = 0.4;
        return pow(2, -10 * x) * sin((x - factor / 4) * (2 * PI) / factor) + 1;
    }
}
