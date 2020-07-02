package be.ugent.blok2.aspects;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSessionEvent;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {
    /*
    * To separate crosscutting concerns (such as logging) as much as possible from the rest of the codebase
    * Aspect Oriented Programming (AOP) is used.
    * For more general information about AOP see https://en.wikipedia.org/wiki/Aspect-oriented_programming and
    * https://www.baeldung.com/spring-aop.
    * AOP makes use of Pointcuts and advices. Pointcuts are used to make a selection of methods, advices are something you want
    * to do before, after or around execution of these methods. In the case of this aspect: logging
    */

    // this logger has extra configuration in the logback-spring.xml file found in the resources folder
    // the most important one being that log messages also get written to a logfile in addition to the terminal
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // voor incoming requests enkel
    //private final Logger loggerIcoming = LoggerFactory.getLogger("requests");

    // makes a pointcut of all functions in the RestControllers
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) && !execution(* countReservedSeatsOfLocation(..))")
    public void allRestControllerPointCut(){
    }

    // makes a pointcut of all methods in RestControllers of all methods in a class annotated with @Service ( primarily the dao classes)
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Service *)")
    public void allMethodsOfControllersAndServices(){

    }


    // advice that logs what function of the RestControllers is called and with what arguments
    @Before("allRestControllerPointCut()")
    public void logBeforeControllerActionsAdvice(JoinPoint joinPoint){
        logger.info("Called {}.{}() with argument[s] = {} ", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
    }

    // advice that logs if an error is thrown in a method
    @AfterThrowing(value = "allMethodsOfControllersAndServices()", throwing = "e")
    public void afterThrowingAdvice(JoinPoint joinPoint, Exception e){
        logger.error("{} for argument[s]: {} in {}.{}(), Error message: {}", e.getClass().getCanonicalName(), Arrays.toString(joinPoint.getArgs()),
                joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), e.getMessage());
    }

    @Pointcut("within(be.ugent.blok2.security.SessionListener)")
    public void sessionPointcut(){
    }

    // advice that logs session methods
    @Before("sessionPointcut()")
    public void sessionLoggingAdvice(JoinPoint jp){
        HttpSessionEvent e = (HttpSessionEvent)  jp.getArgs()[0];
        logger.info("{} : {}", jp.getSignature().getName(), e.getSession().getId());
        //loggerIcoming.info("{} : {}", jp.getSignature().getName(), e.getSession().getId());
    }
}
