/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 * 
 * Copyright 2019,2020 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 */
package de.cau.cs.kieler.klighd.lsp.launch

import com.google.inject.Guice
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.Channels
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.function.Consumer
import org.apache.log4j.Logger
import org.eclipse.elk.core.util.Maybe
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.xtext.ide.server.LanguageServerImpl

/**
 * Extensible entry point for the language server applications for the projects based around KLighD diagrams.<br>
 * Expects none or host and port as arguments. If none the language server is started via<br>
 * stdin/out, otherwise it is started via a socket connection on specified host and port. If only one argument<br>
 * is provided it will be interpreted as port and host will be localhost<br>
 * <br>
 * <b>Note:</b> On MacOS X make sure to add "-Djava.awt.headless=true" to the vmargs!
 * Otherwise the application will freeze!<br>
 * <br>
 * To start this as a plain Java application together with other plug-ins, make sure to add those projects and their
 * project folders into the classpath.
 * 
 * @see LanguageServerLauncher
 * 
 * @author sdo, nre
 */
abstract class AbstractLanguageServer implements Runnable {
    
    static val defaultHost = "localhost"
    
    static val LOG = Logger.getLogger(LanguageServer)
    
    extension ILanguageRegistration languageRegistration
    
    extension ILsCreator creator
    
    /**
     * Queue to execute SWT or AWT calls on the main Thread.
     */
    static val BlockingQueue<Consumer<Void>> mainThreadQueue = new LinkedBlockingQueue<Consumer<Void>>()

    /**
     * Configure this the launch of this language server with the language registration, a language server creator and
     * the instance of the calling class.
     * 
     * @param languageRegistration The registration used to register all languages for this server.
     * @param lsCreator The helping creator class to create this language server.
     */
    def configureAndRun(ILanguageRegistration languageRegistration, ILsCreator lsCreator) {
        this.languageRegistration = languageRegistration
        this.creator = lsCreator
        // Start LS an new Thread to use this as UI Thread.
        new Thread([
            run
        ]).start()
        while (true) {
            synchronized (mainThreadQueue) {
                // Wait until a new function arrives
                while (mainThreadQueue.isNullOrEmpty) {
                    mainThreadQueue.wait
                }
                // Get new element of queue and only empty after it was processed to make sure that waiting clients
                // are not able to continue before the function is executed.
                val f = mainThreadQueue.peek
                if (f !== null) {
                    f.accept(null)
                    mainThreadQueue.poll
                    mainThreadQueue.notify
                }
            }
        }
    }
    
    /**
     * Add a new function to be executed on the main Thread.
     * This method will wait until the function is executed.
     */
    static def addToMainThreadQueue(Consumer<Object> f) {

        val Maybe<Throwable> maybeThrowable = new Maybe
        synchronized (mainThreadQueue) {
            mainThreadQueue.add([
                // Make sure methods in the main thread queue do not throw, any throwable thrown by the given function
                // is remembered and finally re-thrown by this method.
                try {
                    f.accept(null)
                } catch (Throwable t) {
                    maybeThrowable.set(t)
                }
            ])
            // Wait to continue
            while (!mainThreadQueue.isNullOrEmpty) {
                mainThreadQueue.notify
                mainThreadQueue.wait
            }
        }
        if (maybeThrowable.get instanceof Throwable) {
            throw maybeThrowable.get
        }
    }
    
    /**
     * Starts the language server.
     */
    override run() {
        var host = System.getProperty("host")
        var portArg = System.getProperty("port")
        if (portArg !== null) {
            // debug case, communicate via socket
            if (host === null) {
                host = defaultHost
            }
            var port = 0
            try {
                port = Integer.parseInt(portArg)
            } catch (NumberFormatException e) {
                println(e.stackTrace)
                return
            }
            LOG.info("Connection to: " + host + ":" + port)
            // Register all languages
            LOG.info("Starting language server socket")
            bindAndRegisterLanguages()
            
            val serverSocket = AsynchronousServerSocketChannel.open.bind(new InetSocketAddress(host, port))
            val threadPool = Executors.newCachedThreadPool()
            
            LOG.info("Language Server Socket ready!")
            
            while (true) {
                val socketChannel = serverSocket.accept.get            
                val in = Channels.newInputStream(socketChannel)
                val out = Channels.newOutputStream(socketChannel)
                val injector = Guice.createInjector(createLSModules(true))
                val ls = injector.getInstance(LanguageServerImpl)
                LOG.info("Starting language server for client " + socketChannel.remoteAddress)
                buildAndStartLS(injector, ls, in, out, threadPool, [it], true)
                LOG.info("Finished language server for client " + socketChannel.remoteAddress)
            }
        } else {
            LanguageServerLauncher.launch(languageRegistration, creator)
        }
    }
    
}