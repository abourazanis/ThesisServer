#include "decrypter.hpp"
#include <string.h>
#include <openssl/evp.h>
#include <android/log.h>

std::vector<unsigned char> openssl_decrypt (unsigned char *key,
               unsigned char *iv,
               unsigned char *encryptedData,
               int encryptedLength);


class dec: public decrypter{
  
public:
  
  virtual std::vector<unsigned char> decrypt(unsigned char* data, const char* id, int dataSize){
    
    const char key[] = "DECRYPTION_ID";
    int len = strlen(id);
    int lenK = strlen(key);
    std::vector<unsigned char> decData;
    if(len == lenK)
    {
      if(strcmp(key,id) != 0)
	return decData;//invalid key
	
	
	//retrieve iv
	unsigned char*  iv = (unsigned char*)malloc(sizeof(unsigned char)*16);
	int i=0;
	for(i=0;i<16;i++){
	  iv[i] = data[i];
	}
	
	//retrieve salt
	unsigned char*  salt = (unsigned char*)malloc(sizeof(unsigned char)*8);
	for(i=0;i<8;i++){
	  salt[i] = data[16+i];
	}
	
	unsigned char * encdata = (unsigned char*)malloc(sizeof(unsigned char)*(dataSize-(16+8)));
	for(i=0;i<dataSize-(16+8);i++){
	  encdata[i] = data[i+16+8];
	}
	
	unsigned char keyF[32];
	
	int x = EVP_BytesToKey(EVP_aes_256_cbc(), EVP_md5(), salt, (const unsigned char*)key, strlen(key), 1, keyF, iv);
	decData = openssl_decrypt(keyF,iv,encdata,(dataSize-(16+8)));

	free(iv);
	free(salt);
	free(encdata);

	return decData;
    }
    return decData;//invalid key
  }
};


std::vector<unsigned char> openssl_decrypt (unsigned char *key,
               unsigned char *iv,
               unsigned char *encryptedData,
               int encryptedLength)
{
    // Initialisation
    EVP_CIPHER_CTX *cryptCtx = EVP_CIPHER_CTX_new();
    EVP_CIPHER_CTX_init(cryptCtx);

    int decryptedLength = 0;
    int allocateSize = encryptedLength * sizeof(unsigned char);
    int lastDecryptLength = 0;

    unsigned char *decryptedData = (unsigned char *) malloc (allocateSize);
    memset(decryptedData, 0x00, allocateSize);

    int decryptResult = EVP_DecryptInit_ex(cryptCtx,
        EVP_aes_256_cbc(), NULL, key, iv);



    // EVP_DecryptInit_ex returns 1 if it succeeded.
    if (decryptResult == 1)
    {
        decryptResult = EVP_DecryptUpdate(cryptCtx, decryptedData,
            &decryptedLength, encryptedData, encryptedLength);

        // Cleanup
        if (decryptResult == 1)
        {
            // Stick the final data at the end of the last
            // decrypted data.
            EVP_DecryptFinal_ex(cryptCtx,
                decryptedData + decryptedLength,
                &lastDecryptLength);

            decryptedLength = decryptedLength + lastDecryptLength;
            //decryptedData[decryptedLength - 1] = ' ';
        }
        else
        {
	  __android_log_write(ANDROID_LOG_ERROR, "JNI_DEC", "EVP_DeccryptUpdate failure.");
        }
    }
    else
    {
      __android_log_write(ANDROID_LOG_ERROR, "JNI_DEC", "EVP_DecryptInit_ex failure.");
    }

    EVP_CIPHER_CTX_free(cryptCtx);
    EVP_cleanup();
    
    std::vector<unsigned char> decVector(decryptedLength);
    int i = 0;
    for(i=0;i<decryptedLength;i++){
      //decVector.push_back(decryptedData[i]);
      decVector[i] = decryptedData[i];
    }
	  
    return decVector;
}


// the class factories

extern "C" decrypter* create() {
  return new dec;
}

extern "C" void destroy(decrypter* d) {
  delete d;
}
