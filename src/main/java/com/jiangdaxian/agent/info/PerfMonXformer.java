package com.jiangdaxian.agent.info;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CodeConverter;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class PerfMonXformer implements ClassFileTransformer {
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] transformed = null;

		if("com/jiangdaxian/helloword/controller/PropertiesController".equals(className)) {
			System.out.println("Transforming " + className);
			ClassPool pool = null;
			CtClass cl = null;
			try {
				pool = ClassPool.getDefault();

				cl = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));

				// CtMethod aop_method = pool.get("com.jdktest.instrument.AopMethods").
				// getDeclaredMethod("aopMethod");
				// System.out.println(aop_method.getLongName());

				CodeConverter convert = new CodeConverter();
				System.out.println("gogo:"+cl.getName());
				if (cl.isInterface() == false) {
					CtMethod[] methods = cl.getDeclaredMethods();
					for (int i = 0; i < methods.length; i++) {
						if (methods[i].isEmpty() == false) {
							AOPInsertMethod(methods[i]);
						}
					}
					transformed = cl.toBytecode();
				}
			} catch (Exception e) {
				System.err.println("Could not instrument  " + className + ",  exception : " + e.getMessage());
			} finally {
				if (cl != null) {
					cl.detach();
				}
			}
			return transformed;
		}else {
			return null;
		}
	}

	private void AOPInsertMethod(CtMethod method) throws NotFoundException, CannotCompileException {
		// situation 1:添加监控时间
		System.out.println("jdx:"+method.getName() + ","+method.getLongName());
		if("index".equalsIgnoreCase(method.getName())){
			//添加局部变量，如果不同过addLocalVariable设置，在调用属性时将出现compile error: no such field: startTime
			method.addLocalVariable("startTime", CtClass.longType);
			method.insertBefore("System.out.println(startTime);");
			method.insertBefore("startTime = System.currentTimeMillis();");
			method.insertBefore("System.out.println(\"insert before ......\");");
			method.insertAfter("System.out.println(\"leave " + method.getName() + " and time is :\" + (System.currentTimeMillis() - startTime));");
		}

		method.instrument(new ExprEditor() {
			public void edit(MethodCall m) throws CannotCompileException {
				m.replace("{ long stime = System.currentTimeMillis(); $_ = $proceed($$);System.out.println(\""
						+ m.getClassName() + "." + m.getMethodName()
						+ " cost:\" + (System.currentTimeMillis() - stime) + \" ms\");}");
			}
		});
		// situation 2:在方法体前后语句
		// method.insertBefore("System.out.println(\"enter method\");");
		// method.insertAfter("System.out.println(\"leave method\");");
	}
}