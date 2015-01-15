/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.le.farcek.jbw.code.managers;

import com.google.inject.Inject;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import mn.le.farcek.common.utils.FFileUtils;
import mn.le.farcek.common.utils.FStringUtils;
import mn.le.farcek.jbw.api.IConfig;
import mn.le.farcek.jbw.api.action.IActionRequest;
import mn.le.farcek.jbw.api.action.IActionRequestPart;
import mn.le.farcek.jbw.api.exception.MissingResource;
import mn.le.farcek.jbw.api.managers.IResourceManager;
import mn.le.farcek.jbw.api.resource.ResourceResult;

public class IResourceManagerImpl implements IResourceManager {

    @Inject
    IConfig config;

    @Override
    public ResourceResult create(IActionRequest actionRequest, String partName) throws IOException {
        return create(actionRequest.getPart(partName));
    }

    @Override
    public ResourceResult create(IActionRequestPart actionRequestPart) throws IOException {

        if (actionRequestPart == null)
            return null;

        File dir = config.getDirOfResource();

        return create(actionRequestPart, dir);

    }

    @Override
    public ResourceResult create(IActionRequestPart actionRequestPart, File dir) throws IOException {
        String fileName = actionRequestPart.getSubmittedFileName();
        if (fileName == null || fileName.isEmpty())
            return null;

        String ext = FFileUtils.getExtension(fileName);
        String resourceName = FFileUtils.FileNameGenerator(dir, ext) + (ext == null ? "" : "." + ext);

        try (InputStream is = actionRequestPart.getInputStream()) {
            FFileUtils.write(is, new File(dir, resourceName));
        }

        return new ResourceResult(dir,fileName, resourceName);
    }

    @Override
    public void remove(String resourceName) {
        File dir = config.getDirOfResource();

        if (FStringUtils.notEmpty(resourceName)) {
            File f = new File(dir, resourceName);
            f.delete();
        }
    }

    @Override
    public String resourceUrl(String resourceName) throws MissingResource {
        return config.getContextPath() + config.getPathOfResourceHandler() + "/" + resourceName;
    }

    @Override
    public String imageThumbnailUrl(String resourceName, int w, int h) throws MissingResource {
        return config.getContextPath() + config.getPathOfImageResourceHandler() + "/thumbnails/" + String.format("%dx%d-%s", w, h, resourceName);
    }

    @Override
    public File getThumbnailFile(String resourceName, int w, int h) throws MissingResource {
        if (!hasThumbnail(resourceName, w, h))
            try {
                createThumbnail(resourceName, w, h);
            } catch (IOException ex) {
                throw new MissingResource(ex);
            }
        return new File(getThumbnailDir(), String.format("%dx%d-%s", w, h, resourceName));
    }

    @Override
    public String imageUrl(String resourceName) throws MissingResource {
        return resourceUrl(resourceName);
    }

    File thumbnailDir;

    public File getThumbnailDir() {
        if (thumbnailDir == null) {
            thumbnailDir = new File(config.getDirOfResource(), "thumbnails");
            if (!thumbnailDir.isDirectory())
                thumbnailDir.mkdirs();
        }
        return thumbnailDir;
    }

    public Boolean hasThumbnail(String file, int w, int h) {
        File f = new File(getThumbnailDir(), String.format("%dx%d-%s", w, h, file));
        return f.isFile();
    }

    public synchronized void createThumbnail(String imgFile, int w, int h) throws IOException {
        File imagefile = new File(config.getDirOfResource(), imgFile);
        BufferedImage image = ImageIO.read(imagefile);
        BufferedImage thamnelImage = resizer(image, w, h);

        File img = new File(getThumbnailDir(), String.format("%dx%d-%s", w, h, imgFile));

        ImageIO.write(thamnelImage, "png", img);
    }

    private static BufferedImage resizer(BufferedImage image, int w, int h) throws IOException {
        int type = (image.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        BufferedImage thamnelImage = new BufferedImage(w, h, type);
        Graphics g = thamnelImage.getGraphics();

        float imgW = image.getWidth();
        float imgH = image.getHeight();

        float W = w;
        float H = h;

        float d = Math.min(imgW / W, imgH / H);

        int nW = Math.round(imgW / d);
        int nH = Math.round(imgH / d);

        g.drawImage(image, Math.round((W - nW) / 2), Math.round((H - nH) / 2), nW, nH, null);
        g.dispose();
        return thamnelImage;
    }

}
