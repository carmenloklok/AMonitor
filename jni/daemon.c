#include <unistd.h>
#include <android/log.h>
#include <pthread.h>
#include <semaphore.h>
pthread_t *tid;
sem_t tsem,psem;
int daemon_init(void) 
{ 
	pid_t pid; 
  if((pid = fork()) < 0) 
  	return(-1); 
  else if(pid != 0) 
  {
      __android_log_write(ANDROID_LOG_ERROR,"daemon","daemon parent got killed");   
      exit(0); /* parent exit */ 
  }
  return(0); 
}
void* start_pthread1(){
    __android_log_print(ANDROID_LOG_ERROR,"daemon","thread start");
    sem_init(&tsem,0,0);
    sem_wait(&tsem);
    __android_log_print(ANDROID_LOG_ERROR,"daemon","thread end");
}

int Java_com_rich_service_RegisterSMSService_daemon(void) 
{ 
    int id=0;
	if((id=daemon_init()) == -1) 
  { 
  	exit(0); 
  } 
    __android_log_print(ANDROID_LOG_ERROR,"daemon","daemon running");
    sem_init(&tsem,0,0);
    sem_wait(&tsem);
    __android_log_print(ANDROID_LOG_ERROR,"daemon","daemon end");
    return(0); 
}

