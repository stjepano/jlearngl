package dev.stjepano.platform.processor;

import com.google.auto.service.AutoService;
import dev.stjepano.platform.processor.anno.GenerateNativeBindings;
import dev.stjepano.platform.processor.anno.NativeBinding;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("dev.stjepano.platform.processor.anno.GenerateNativeBindings")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class GenerateNativeBindingsProcessor extends AbstractProcessor {

    private static class IndentWriter extends PrintWriter {

        private final String indent;
        private int indentLevel = 0;

        public IndentWriter(Writer out, int indentSpaces) {
            super(out);
            indent = " ".repeat(indentSpaces);
        }

        public void increaseIndent() {
            this.indentLevel++;
        }

        public void decreaseIndent() {
            this.indentLevel--;
        }

        public void printIndent() {
            super.print(this.indent.repeat(this.indentLevel));
        }
    }

    record MethodParam(String name, String type) { }
    record Method(String name, String returnType, MethodParam[] params) { }

    private final List<Method> methods = new ArrayList<>(16);

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(GenerateNativeBindings.class)) {
            if (element instanceof TypeElement typeElement) {
                processInterface(typeElement);
            }
        }
        return true;
    }

    private void processInterface(TypeElement element) {
        findMethods(element);

        var annotation = element.getAnnotation(GenerateNativeBindings.class);
        String targetClassName = annotation.className();

        String packageName;
        var targetPackage = annotation.targetPackage();
        if (targetPackage.isBlank()) {
            var pkg = processingEnv.getElementUtils().getPackageOf(element);
            packageName = pkg.getQualifiedName().toString();
        } else {
            packageName = targetPackage.trim();
        }

        String classAccess = annotation.classAccess();

        try {
            generateClass(classAccess, packageName, targetClassName);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate " + packageName + "." + targetClassName + ": " + e.getMessage());
        }
    }

    private void findMethods(TypeElement element) {
        this.methods.clear();
        for (Element enclosed : element.getEnclosedElements()) {
            if (enclosed instanceof ExecutableElement method) {
                if (method.getAnnotation(NativeBinding.class) != null) {
                    String methodName = method.getSimpleName().toString();
                    String returnType = method.getReturnType().toString();
                    List<MethodParam> params = method.getParameters().stream()
                            .map(variableElement -> {
                                String name = variableElement.getSimpleName().toString();
                                String type = variableElement.asType().toString();
                                return new MethodParam(name, type);
                            })
                            .toList();

                    this.methods.add(new Method(methodName, returnType, params.toArray(new MethodParam[0])));
                }
            }
        }
    }

    private void generateClass(String classAccess, String packageName, String targetClassName) throws IOException {
        JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + targetClassName);

        try (IndentWriter pw = new IndentWriter(file.openWriter(), 4)) {

            pw.println("package " + packageName + ";\n");

            String imports = """
                    import java.lang.foreign.*;
                    import java.lang.invoke.MethodHandle;
                    import java.util.NoSuchElementException;
                    """;

            pw.println(imports);

            String classAccessWithSpace = classAccess.isBlank() ? "" : classAccess + " ";
            pw.println("// Generated code do not modify\n" + classAccessWithSpace + "final class " + targetClassName + " {\n");
            pw.increaseIndent();

            buildMethodHandles(pw);

            pw.println();

            buildInit(pw);

            pw.println();

            buildImplementations(pw);

            pw.println();

            pw.println("}\n");
            pw.decreaseIndent();
        }
    }

    private void buildImplementations(IndentWriter pw) {
        for (Method method : this.methods) {
            pw.printIndent();
            pw.print("public static " + method.returnType + " " + method.name + "(");
            boolean first = true;
            for (MethodParam param : method.params) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print(param.type + " " + param.name);
            }
            pw.println(") {");
            pw.increaseIndent();

            pw.printIndent();
            pw.println("try {");
            pw.increaseIndent();
            pw.printIndent();
            if (!method.returnType.equals("void")) {
                pw.print("return (" + method.returnType + ") ");
            }
            pw.print("h_" + method.name + ".invokeExact(");
            first = true;
            for (MethodParam param : method.params) {
                if (!first) {
                    pw.print(", ");
                }
                first = false;
                pw.print(param.name);
            }
            pw.println(");");
            pw.decreaseIndent();
            pw.printIndent();
            pw.println("} catch (Throwable e) {");
            pw.increaseIndent();
            pw.printIndent();
            pw.println("throw new RuntimeException(e);");
            pw.decreaseIndent();
            pw.printIndent();
            pw.decreaseIndent();
            pw.println("}");


            pw.printIndent();
            pw.println("}\n");
        }
    }

    private void buildMethodHandles(IndentWriter pw) {
        for (var method : this.methods) {
            pw.printIndent();
            pw.println("private static MethodHandle h_" + method.name + ";");
        }
    }

    private void buildInit(IndentWriter pw) {
        pw.printIndent();
        pw.println("private static MethodHandle findFunction(Linker linker, SymbolLookup symbolLookup, String functionName, FunctionDescriptor functionDescriptor) {");
        pw.increaseIndent();
        pw.printIndent();
        pw.println("return linker.downcallHandle(");
        pw.increaseIndent();
        pw.printIndent();
        pw.println("symbolLookup.find(functionName).orElseThrow(() -> new NoSuchElementException(\"Function \" + functionName + \" not found in library.\")),");
        pw.printIndent();
        pw.println("functionDescriptor);");
        pw.decreaseIndent();
        pw.decreaseIndent();
        pw.printIndent();
        pw.println("}\n");


        pw.printIndent();
        pw.println("public static void init(Linker linker, SymbolLookup symbolLookup) {");
        pw.increaseIndent();

        for (Method method : this.methods) {
            pw.printIndent();
            pw.println("h_" + method.name + " = findFunction(linker, symbolLookup, \"" + method.name + "\", " + functionDescriptor(method) + ");");
        }

        pw.decreaseIndent();
        pw.printIndent();
        pw.println("}");

    }

    private String functionDescriptor(Method method) {
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        if (method.returnType.equals("void")) {
            sb.append("FunctionDescriptor.ofVoid(");
        } else {
            sb.append("FunctionDescriptor.of(");
            sb.append(valueLayout(method.returnType));
            first = false;
        }
        for (MethodParam param : method.params) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(valueLayout(param.type));
        }
        sb.append(")");
        return sb.toString();
    }

    private String valueLayout(String returnType) {
        return switch (returnType) {
            case "byte" -> "ValueLayout.JAVA_BYTE";
            case "boolean" -> "ValueLayout.JAVA_BOOLEAN";
            case "short" -> "ValueLayout.JAVA_SHORT";
            case "int" -> "ValueLayout.JAVA_INT";
            case "long" -> "ValueLayout.JAVA_LONG";
            case "float" -> "ValueLayout.JAVA_FLOAT";
            case "double" -> "ValueLayout.JAVA_DOUBLE";
            case "java.lang.foreign.MemorySegment" -> "ValueLayout.ADDRESS";
            default -> "ValueLayout.UNKNOWN";
        };
    }

}
