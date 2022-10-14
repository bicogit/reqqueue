package com.bico.reqqueue.aspectj;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import com.bico.reqqueue.utils.StringUtil;

public abstract class AbstractReqQueueAspectSupport {

	public static int availableProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	public static Method extractHandleHandlerMethod(ProceedingJoinPoint pjp,String queueName,String name, Class<?>[] locationClass) {
		if (StringUtil.isBlank(name)) {
			return null;
		}

		boolean mustStatic = locationClass != null && locationClass.length >= 1;
		Class<?> clazz;
		if (mustStatic) {
			clazz = locationClass[0];
		} else {
			// By default current class.
			clazz = pjp.getTarget().getClass();
		}
		MethodWrapper m = ResourceMetadataRegistry.lookupHandleHandler(queueName);
		if (m == null) {
			// First time, resolve the block handler.
			Method method = resolveHandleHandlerInternal(pjp, name, clazz, mustStatic);
			// Cache the method instance.
			ResourceMetadataRegistry.updateHandleFor(queueName, method);
			return method;
		}
		if (!m.isPresent()) {
			return null;
		}
		
		return m.getMethod();
	}

	public static Method resolveHandleHandlerInternal(ProceedingJoinPoint pjp, /* @NonNull */ String name, Class<?> clazz,
			boolean mustStatic) {
		Method originMethod = resolveMethod(pjp);
		Class<?>[] originList = originMethod.getParameterTypes();
		Class<?>[] parameterTypes = Arrays.copyOf(originList, originList.length + 1);
		parameterTypes[parameterTypes.length - 1] = ReqWrapper.class;
		return findMethod(mustStatic, clazz, name, originMethod.getReturnType(), parameterTypes);
	}

	public static Method resolveMethod(ProceedingJoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Class<?> targetClass = joinPoint.getTarget().getClass();

		Method method = getDeclaredMethodFor(targetClass, signature.getName(),
				signature.getMethod().getParameterTypes());
		if (method == null) {
			throw new IllegalStateException("Cannot resolve target method: " + signature.getMethod().getName());
		}
		return method;
	}

	/**
	 * Get declared method with provided name and parameterTypes in given class and
	 * its super classes. All parameters should be valid.
	 *
	 * @param clazz          class where the method is located
	 * @param name           method name
	 * @param parameterTypes method parameter type list
	 * @return resolved method, null if not found
	 */
	public static Method getDeclaredMethodFor(Class<?> clazz, String name, Class<?>... parameterTypes) {
		try {
			return clazz.getDeclaredMethod(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null) {
				return getDeclaredMethodFor(superClass, name, parameterTypes);
			}
		}
		return null;
	}
	
	public static boolean checkStatic(boolean mustStatic, Method method) {
		return !mustStatic || isStatic(method);
	}

	public static Object invoke(ProceedingJoinPoint pjp, Method method, Object[] args) throws Throwable {
		try {
			if (!method.isAccessible()) {
				makeAccessible(method);
			}
			if (isStatic(method)) {
				return method.invoke(null, args);
			}
			return method.invoke(pjp.getTarget(), args);
		} catch (InvocationTargetException e) {
			// throw the actual exception
			throw e.getTargetException();
		}
	}

	/**
	 * Make the given method accessible, explicitly setting it accessible if
	 * necessary. The {@code setAccessible(true)} method is only called when
	 * actually necessary, to avoid unnecessary conflicts with a JVM SecurityManager
	 * (if active).
	 * 
	 * @param method the method to make accessible
	 * @see java.lang.reflect.Method#setAccessible
	 */
	private static void makeAccessible(Method method) {
		boolean isNotPublic = !Modifier.isPublic(method.getModifiers())
				|| !Modifier.isPublic(method.getDeclaringClass().getModifiers());
		if (isNotPublic && !method.isAccessible()) {
			method.setAccessible(true);
		}
	}

	public static boolean isStatic(Method method) {
		return Modifier.isStatic(method.getModifiers());
	}

	public static Method findMethod(boolean mustStatic, Class<?> clazz, String name, Class<?> returnType,Class<?>... parameterTypes) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if (name.equals(method.getName()) && checkStatic(mustStatic, method)
					&& returnType.isAssignableFrom(method.getReturnType())
					&& Arrays.equals(parameterTypes, method.getParameterTypes())) {

				//RecordLog.info("Resolved method [{}] in class [{}]", name, clazz.getCanonicalName());
				return method;
			}
		}
		// Current class not found, find in the super classes recursively.
		Class<?> superClass = clazz.getSuperclass();
		if (superClass != null && !Object.class.equals(superClass)) {
			return findMethod(mustStatic, superClass, name, returnType, parameterTypes);
		} else {
			String methodType = mustStatic ? " static" : "";
			//RecordLog.warn("Cannot find{} method [{}] in class [{}] with parameters {}", methodType, name,
					//clazz.getCanonicalName(), Arrays.toString(parameterTypes));
			return null;
		}
	}
}
