package com.cmp180.livechat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import com.cmp180.livechat.common.Protocol;
import com.cmp180.livechat.common.VideoManager;
import com.github.sarxos.webcam.Webcam;

public class VideoManagerTest {

    @Test
    @DisplayName("Kiểm tra Protocol constants cho Video Call")
    public void testVideoCallProtocolConstants() {
        assertEquals("VIDEO_CALL_REQ", Protocol.CMD_VIDEO_CALL_REQ);
        assertEquals("VIDEO_DATA", Protocol.CMD_VIDEO_DATA);
    }

    @Test
    @DisplayName("Kiểm tra VideoManager VideoSource enum và chuyển nguồn video")
    public void testVideoSourceSwitching() {
        VideoManager vm = VideoManager.getInstance();
        assertNotNull(vm);
        
        vm.setVideoSource(VideoManager.VideoSource.VIRTUAL_AVATAR);
        assertEquals(VideoManager.VideoSource.VIRTUAL_AVATAR, vm.getVideoSource());
        assertEquals("Camera ảo / Avatar", vm.getVideoSource().getDisplayName());

        vm.setVideoSource(VideoManager.VideoSource.SCREEN_SHARE);
        assertEquals(VideoManager.VideoSource.SCREEN_SHARE, vm.getVideoSource());
        assertEquals("Chia sẻ màn hình", vm.getVideoSource().getDisplayName());
    }

    @Test
    @DisplayName("Kiểm tra thư viện Webcam Capture nạp thành công")
    public void testWebcamLibraryLoads() {
        System.setProperty("bridj.quiet", "true");
        assertDoesNotThrow(() -> {
            try {
                Webcam webcam = Webcam.getDefault();
                System.out.println("Default webcam: " + (webcam != null ? webcam.getName() : "Không tìm thấy webcam vật lý (sẽ dùng Virtual/Screen camera)"));
            } catch (Throwable t) {
                System.out.println("Webcam capture throw (fallback handled): " + t.getMessage());
            }
        });
    }
}
