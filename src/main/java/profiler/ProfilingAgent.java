package profiler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

// TODO: add insertion of try-finally block
public class ProfilingAgent implements ClassFileTransformer {
    public static void premain(String args, Instrumentation inst) throws IOException {
        FileWriter fileWriter = new FileWriter("out/out.txt");
        fileWriter.close();
        inst.addTransformer(new ProfilingAgent());
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.startsWith("org/jetbrains/test")) {
            ClassReader cr = new ClassReader(classfileBuffer);
            // TODO: compute maxs and frames manually
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            // uncomment for debugging
//            TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
//            cr.accept(new AddProfilerClassVisitor(cv), 0);
            cr.accept(new AddProfilerClassVisitor(cw), 0);

            return cw.toByteArray();
        }
        return null;
    }
}

class AddProfilerClassVisitor extends ClassVisitor {
    AddProfilerClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            mv = new AddProfilerMethodVisitor(access, name, desc, mv);
        }
        return mv;
    }
}

class AddProfilerMethodVisitor extends AdviceAdapter {
    private int state;

    public AddProfilerMethodVisitor(int access, String name, String desc,
                                    MethodVisitor mv) {
        super(Opcodes.ASM5, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
        mv.visitMethodInsn(INVOKESTATIC, "profiler/Profiler",
                "methodStart", "()Lprofiler/State;", false);
        // TODO: check is it correct to use `LONG_TYPE` for object
        state = newLocal(Type.LONG_TYPE);
        mv.visitVarInsn(ASTORE, state);
    }

    @Override
    protected void onMethodExit(int opcode) {
        mv.visitVarInsn(ALOAD, state);
        mv.visitMethodInsn(INVOKEVIRTUAL, "profiler/State", "methodFinish", "()V", false);
    }
}