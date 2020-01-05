package com.mycomany.community.util;



public interface communityConstant {

   // Activation success
   int ACTIVATION_SUCCESS=0;

   int ACTIVATION_REPECT=1;

   int ACTIVATION_FAILURE=2;

   //default expired time for ticket.
   int DEFAULT_EXPIRED_SECONDS= 3600*12;

   // save expired time for ticket
   int REMEMBER_EXPIRED_SECONDS= 3600*12*100;

   // set entity type

   int ENTITY_TYPE_POST = 1;

   // set entity comment

   int ENTITY_TYPE_COMMENT=2;

   // set entity user
   int ENTITY_TYPE_USER =3;

   // TOPIC commend
   String TOPIC_COMMENT ="comment";

   //Topic Like
   String TOPIC_LIKE ="like";

   //Topic follow
   String TOPIC_FOLLOW ="follow";

   // System user Id
   int SYSTEM_USER_ID = 1;

}
