/*
 * Copyright 2003,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.core;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.objectweb.asm.ClassReader;

/**
 * Abstract class for all code-generating CGLIB utilities. In addition to
 * caching generated classes for performance, it provides hooks for customizing
 * the <code>ClassLoader</code>, name of the generated class, and
 * transformations applied before generation.
 */
abstract public class AbstractClassGenerator implements ClassGenerator {
	protected static class Source {
		Map		cache	= new WeakHashMap();
		String	name;

		public Source(final String name) {
			this.name = name;
		}
	}

	private static final ThreadLocal	CURRENT		= new ThreadLocal();

	private static final Object			NAME_KEY	= new Object();

	/**
	 * Used internally by CGLIB. Returns the <code>AbstractClassGenerator</code>
	 * that is being used to generate a class in the current thread.
	 */
	public static AbstractClassGenerator getCurrent() {
		return (AbstractClassGenerator) CURRENT.get();
	}

	private boolean				attemptLoad;
	private ClassLoader			classLoader;
	private String				className;
	private Object				key;
	private String				namePrefix;
	private NamingPolicy		namingPolicy	= DefaultNamingPolicy.INSTANCE;
	private final Source		source;

	private GeneratorStrategy	strategy		= DefaultGeneratorStrategy.INSTANCE;

	private boolean				useCache		= true;

	protected AbstractClassGenerator(final Source source) {
		this.source = source;
	}

	public boolean getAttemptLoad() {
		return attemptLoad;
	}

	public ClassLoader getClassLoader() {
		ClassLoader t = classLoader;
		if (t == null) {
			// t = getDefaultClassLoader();
		}
		if (t == null) {
			t = getClass().getClassLoader();
		}
		if (t == null) {
			t = Thread.currentThread().getContextClassLoader();
		}
		if (t == null) {
			throw new IllegalStateException("Cannot determine classloader");
		}
		return t;
	}

	/**
	 * @see #setNamingPolicy
	 */
	public NamingPolicy getNamingPolicy() {
		return namingPolicy;
	}

	/**
	 * @see #setStrategy
	 */
	public GeneratorStrategy getStrategy() {
		return strategy;
	}

	/**
	 * @see #setUseCache
	 */
	public boolean getUseCache() {
		return useCache;
	}

	/**
	 * If set, CGLIB will attempt to load classes from the specified
	 * <code>ClassLoader</code> before generating them. Because generated
	 * class names are not guaranteed to be unique, the default is
	 * <code>false</code>.
	 */
	public void setAttemptLoad(final boolean attemptLoad) {
		this.attemptLoad = attemptLoad;
	}

	/**
	 * Set the <code>ClassLoader</code> in which the class will be generated.
	 * Concrete subclasses of <code>AbstractClassGenerator</code> (such as
	 * <code>Enhancer</code>) will try to choose an appropriate default if
	 * this is unset.
	 * <p>
	 * Classes are cached per-<code>ClassLoader</code> using a
	 * <code>WeakHashMap</code>, to allow the generated classes to be removed
	 * when the associated loader is garbage collected.
	 * 
	 * @param classLoader the loader to generate the new class with, or null to
	 *            use the default
	 */
	public void setClassLoader(final ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Override the default naming policy.
	 * 
	 * @see DefaultNamingPolicy
	 * @param namingPolicy the custom policy, or null to use the default
	 */
	public void setNamingPolicy(NamingPolicy namingPolicy) {
		if (namingPolicy == null) {
			namingPolicy = DefaultNamingPolicy.INSTANCE;
		}
		this.namingPolicy = namingPolicy;
	}

	/**
	 * Set the strategy to use to create the bytecode from this generator. By
	 * default an instance of {@see DefaultGeneratorStrategy} is used.
	 */
	public void setStrategy(GeneratorStrategy strategy) {
		if (strategy == null) {
			strategy = DefaultGeneratorStrategy.INSTANCE;
		}
		this.strategy = strategy;
	}

	/**
	 * Whether use and update the static cache of generated classes for a class
	 * with the same properties. Default is <code>true</code>.
	 */
	public void setUseCache(final boolean useCache) {
		this.useCache = useCache;
	}

	private String getClassName(final ClassLoader loader) {
		final Set nameCache = getClassNameCache(loader);
		return namingPolicy.getClassName(
				namePrefix,
				source.name,
				key,
				new Predicate() {
					public boolean evaluate(final Object arg) {
						return nameCache.contains(arg);
					}
				});
	}

	private Set getClassNameCache(final ClassLoader loader) {
		return (Set) ((Map) source.cache.get(loader)).get(NAME_KEY);
	}

	protected Object create(final Object key) {
		try {
			Class gen = null;

			synchronized (source) {
				final ClassLoader loader = getClassLoader();
				Map cache2 = null;
				cache2 = (Map) source.cache.get(loader);
				if (cache2 == null) {
					cache2 = new HashMap();
					cache2.put(NAME_KEY, new HashSet());
					source.cache.put(loader, cache2);
				} else if (useCache) {
					final Reference ref = (Reference) cache2.get(key);
					gen = (Class) ((ref == null) ? null : ref.get());
				}
				if (gen == null) {
					final Object save = CURRENT.get();
					CURRENT.set(this);
					try {
						this.key = key;

						if (attemptLoad) {
							try {
								gen = loader.loadClass(getClassName());
							} catch (final ClassNotFoundException e) {
								// ignore
							}
						}
						if (gen == null) {
							final byte[] b = strategy.generate(this);
							final String className = ClassNameReader.getClassName(new ClassReader(
									b));
							getClassNameCache(loader).add(className);
							gen = ReflectUtils.defineClass(className, b, loader);
						}

						if (useCache) {
							cache2.put(key, new WeakReference(gen));
						}
						return firstInstance(gen);
					} finally {
						CURRENT.set(save);
					}
				}
			}
			return firstInstance(gen);
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Error e) {
			throw e;
		} catch (final Exception e) {
			throw new CodeGenerationException(e);
		}
	}

	abstract protected Object firstInstance(Class type) throws Exception;

	final protected String getClassName() {
		if (className == null) {
			className = getClassName(getClassLoader());
		}
		return className;
	}

	abstract protected ClassLoader getDefaultClassLoader();

	abstract protected Object nextInstance(Object instance) throws Exception;

	protected void setNamePrefix(final String namePrefix) {
		this.namePrefix = namePrefix;
	}
}
