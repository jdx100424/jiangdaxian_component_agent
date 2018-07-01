package com.jiangdaxian.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

import com.jiangdaxian.agent.info.PerfMonXformer;

public class MyAgent {
	private static Instrumentation inst = null;
	/**
	 * 该方法在main方法之前运行，与main方法运行在同一个JVM中 并被同一个System ClassLoader装载 被统一的安全策略(security
	 * policy)和上下文(context)管理
	 *
	 * @param agentOps
	 * @param inst
	 * @author SHANHY
	 * @create 2016年3月30日
	 */
	public static void premain(String agentOps, Instrumentation _inst) {
		System.out.println("PerfMonAgent.premain() was called.");
		// Initialize the static variables we use to track information.
		inst = _inst;
		// Set up the class-file transformer.
		ClassFileTransformer trans = new PerfMonXformer();
		System.out.println("Adding a PerfMonXformer instance to the JVM.");
		inst.addTransformer(trans);
		System.out.println("Adding a PerfMonXformer instance to the JVM end.");
	}

	/**
	 * 如果不存在 premain(String agentOps, Instrumentation inst) 则会执行 premain(String
	 * agentOps)
	 *
	 * @param agentOps
	 * @author SHANHY
	 * @create 2016年3月30日
	 */
	public static void premain(String agentOps) {
		System.out.println("=========premain方法执行2========");
		System.out.println(agentOps);
	}
}
