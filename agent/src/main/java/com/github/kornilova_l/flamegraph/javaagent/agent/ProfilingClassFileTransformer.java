package com.github.kornilova_l.flamegraph.javaagent.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

class ProfilingClassFileTransformer implements ClassFileTransformer {

    private static boolean hasSystemCLInChain(ClassLoader loader) {
        ClassLoader chainLoader = loader;
        while (chainLoader != null) {
            if (chainLoader == ClassLoader.getSystemClassLoader()) {
                return true;
            }
            chainLoader = chainLoader.getParent();
        }
        return false;
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.startsWith("java") &&
                !className.startsWith("sun") &&
                (className.startsWith("com/github/kornilova_l/samples") || !className.startsWith("com/github/kornilova_l")) &&
                !className.startsWith("jdk") &&
                !className.startsWith("com/sun")) {
//            List<MethodConfig> methodConfigs = AgentConfigurationManager.findIncludingConfigs(className);
//            if (methodConfigs.size() != 0) {
                ClassReader cr = new ClassReader(classfileBuffer);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
                // uncomment for debugging
//            TraceClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
                // SKIP_FRAMES avoids visiting frames that will be ignored and recomputed from scratch in the class writer.
                cr.accept(new ProfilingClassVisitor(cw, className, hasSystemCLInChain(loader)), ClassReader.SKIP_FRAMES);
                return cw.toByteArray();
//            }
        }
        return null;
    }
}