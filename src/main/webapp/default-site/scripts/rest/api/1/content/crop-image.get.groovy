import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import scripts.api.ContentServices
import javax.imageio.ImageIO

def req = request 
def site = params.site
def imgPath = params.path
def t = params.t.toInteger()
def l = params.l.toInteger()
def w = params.w.toInteger()
def h = params.h.toInteger()

def imgToCrop = null
def imgCropped = null
def imgCroppedOutStream = null
def imgCroppedInStream = null

def imgType = imgPath.substring(imgPath.indexOf(".")+1)
def imgPathOnly = imgPath.substring(0, imgPath.lastIndexOf("/"))
def imgFilename = imgPath.substring(imgPath.lastIndexOf("/")+1)

def context = ContentServices.createContext(applicationContext, request)

imgToCrop = ImageIO.read(ContentServices.getContentAsStream(site, imgPath, context))

imgCropped = imgToCrop.getSubimage(l, t, w, h)
imgCroppedOutStream = new ByteArrayOutputStream()

ImageIO.write(imgCropped, imgType, imgCroppedOutStream) 
imgCroppedInStream = new ByteArrayInputStream(imgCroppedOutStream.toByteArray())

def result = ContentServices.writeContentAsset(context, site, imgPathOnly, imgFilename, imgCroppedInStream, "true", "", "", "", "false", "true", null);
return result