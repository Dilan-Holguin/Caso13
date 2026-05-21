package com.eap08.domesticas.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageControllerTest {

    private final PageController controller = new PageController();

    @Test
    void shouldRenderResetPasswordPageWithEscapedToken() {
        String html = controller.resetPasswordPage("<script>alert('x')</script>");

        assertThat(html).contains("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;");
        assertThat(html).contains("id=\"token\" value=\"&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;\"");
    }

    @Test
    void shouldRenderJoinPageWithEscapedToken() {
        String html = controller.joinPage("<script>alert('x')</script>");

        assertThat(html).contains("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;");
        assertThat(html).contains("id=\"token\" value=\"&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;\"");
    }
}