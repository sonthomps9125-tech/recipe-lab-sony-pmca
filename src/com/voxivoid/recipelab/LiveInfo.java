package com.voxivoid.recipelab;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;

/**
 * Connects to Sony Scalar CameraEx via reflection, registering live listeners for:
 * - ShutterSpeedChangeListener (currentShutterSpeed_n, currentShutterSpeed_d)
 * - ApertureChangeListener (currentAperture)
 * and querying ISOSensitivity.
 */
public class LiveInfo {
    private static final String EX = "com.sony.scalar.hardware.CameraEx$";

    private Object ex;
    private Runnable changed;

    private int shutterN = 0;
    private int shutterD = 0;
    private int aperture = 0;
    private int iso = -1;
    private float ev = Float.NaN;

    private boolean shutterHeard = false;
    private boolean apertureHeard = false;

    public LiveInfo() {}

    public void attach(Object cameraEx, Runnable onChange) {
        detach();
        this.ex = cameraEx;
        this.changed = onChange;
        if (cameraEx == null) return;

        hook("setShutterSpeedChangeListener", "ShutterSpeedChangeListener");
        hook("setApertureChangeListener", "ApertureChangeListener");

        readParameters(cameraEx);
    }

    public void detach() {
        if (ex != null) {
            unhook("setShutterSpeedChangeListener", "ShutterSpeedChangeListener");
            unhook("setApertureChangeListener", "ApertureChangeListener");
        }
        ex = null;
        changed = null;
    }

    private void hook(String setterName, final String listenerInterfaceName) {
        try {
            Class<?> listenerClass = Class.forName(EX + listenerInterfaceName);
            Object proxy = Proxy.newProxyInstance(
                listenerClass.getClassLoader(),
                new Class<?>[] { listenerClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("onShutterSpeedChange".equals(name) && args != null && args.length > 0) {
                            heardShutter(args[0]);
                        } else if ("onApertureChange".equals(name) && args != null && args.length > 0) {
                            heardAperture(args[0]);
                        } else if ("equals".equals(name)) {
                            return proxy == args[0];
                        } else if ("hashCode".equals(name)) {
                            return System.identityHashCode(proxy);
                        } else if ("toString".equals(name)) {
                            return "LiveInfo listener " + listenerInterfaceName;
                        }
                        return null;
                    }
                }
            );
            Method setter = ex.getClass().getMethod(setterName, listenerClass);
            setter.invoke(ex, proxy);
        } catch (Throwable t) {
            // CameraEx listener not available on this firmware version
        }
    }

    private void unhook(String setterName, String listenerInterfaceName) {
        try {
            Class<?> listenerClass = Class.forName(EX + listenerInterfaceName);
            Method setter = ex.getClass().getMethod(setterName, listenerClass);
            setter.invoke(ex, (Object) null);
        } catch (Throwable t) {}
    }

    private void heardShutter(Object info) {
        int n = intField(info, "currentShutterSpeed_n");
        int d = intField(info, "currentShutterSpeed_d");
        shutterHeard = true;
        shutterN = n;
        shutterD = d;
        fire();
    }

    private void heardAperture(Object info) {
        int a = intField(info, "currentAperture");
        apertureHeard = true;
        aperture = a;
        fire();
    }

    private void fire() {
        if (changed != null) {
            changed.run();
        }
    }

    public void readParameters(Object cameraEx) {
        if (cameraEx == null) return;
        try {
            // Read ISO sensitivity
            Integer iIso = callInt(cameraEx, "getISOSensitivity");
            if (iIso != null) iso = iIso;

            // Read initial shutter speed if not yet heard from listener
            if (!shutterHeard) {
                Object pair = call(cameraEx, "getShutterSpeed");
                if (pair != null) {
                    Integer fn = fieldInt(pair, "first");
                    Integer sd = fieldInt(pair, "second");
                    if (fn != null && sd != null) {
                        shutterN = fn;
                        shutterD = sd;
                    }
                }
            }

            // Read initial aperture if not yet heard from listener
            if (!apertureHeard) {
                Integer iAp = callInt(cameraEx, "getAperture");
                if (iAp != null) aperture = iAp;
            }
        } catch (Throwable t) {}
        fire();
    }

    public void setEv(float evValue) {
        this.ev = evValue;
        fire();
    }

    public String shutter() {
        if (shutterD <= 0 || shutterN <= 0) return "--";
        double sec = (double) shutterN / shutterD;
        if (sec >= 1.0) {
            if (sec == Math.round(sec)) {
                return ((int) sec) + "\"";
            }
            return String.format(Locale.US, "%.1f\"", sec);
        }
        int denom = (int) Math.round((double) shutterD / shutterN);
        return "1/" + denom;
    }

    public String aperture() {
        if (aperture <= 0) return "--";
        double f = aperture / 100.0;
        return String.format(Locale.US, "F%.1f", f);
    }

    public String ev() {
        if (Float.isNaN(ev)) return "--";
        if (Math.abs(ev) < 0.05f) return "±0.0";
        if (ev > 0) return String.format(Locale.US, "+%.1f", ev);
        return String.format(Locale.US, "%.1f", ev);
    }

    public String iso() {
        if (iso < 0) return "--";
        if (iso == 0) return "AUTO";
        return String.valueOf(iso);
    }

    private static int intField(Object obj, String fieldName) {
        Integer v = fieldInt(obj, fieldName);
        return v != null ? v : 0;
    }

    private static Integer fieldInt(Object obj, String fieldName) {
        if (obj == null) return null;
        try {
            Field f = obj.getClass().getField(fieldName);
            Object v = f.get(obj);
            if (v instanceof Number) return ((Number) v).intValue();
        } catch (Throwable t) {}
        return null;
    }

    private static Method find(Object obj, String methodName) {
        if (obj == null) return null;
        for (Method m : obj.getClass().getMethods()) {
            if (m.getName().equals(methodName) && m.getParameterTypes().length == 0) {
                return m;
            }
        }
        return null;
    }

    private static Object call(Object obj, String methodName) {
        try {
            Method m = find(obj, methodName);
            if (m != null) return m.invoke(obj);
        } catch (Throwable t) {}
        return null;
    }

    private static Integer callInt(Object obj, String methodName) {
        Object v = call(obj, methodName);
        if (v instanceof Number) return ((Number) v).intValue();
        return null;
    }
}
