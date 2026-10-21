package com.example.reservation;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** ログイン → 空き枠確認 → 予約 → 重複予約不可 → 取消 → 権限チェック の一連の流れ */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL",
        "app.seed.enabled=true"})
@AutoConfigureMockMvc
class ReservationFlowTest {
    @Autowired
    MockMvc mvc;

    private MockHttpSession login(String loginId, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login_id\":\"" + loginId + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    void 未ログインは401() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login_id\":\"member1\",\"password\":\"wrong\"}")).andExpect(status().isUnauthorized());
    }

    @Test
    void 会員は予約と取消ができ_重複予約と管理APIは拒否される() throws Exception {
        MockHttpSession member = login("member1", "pass1234");

        String slots = mvc.perform(get("/api/slots").session(member))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        int slotId = JsonPath.read(slots, "$.items[0].id");

        String body = "{\"slot_id\":" + slotId + "}";
        MvcResult reserved = mvc.perform(post("/api/reservations").session(member)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn();
        int reservationId = JsonPath.read(reserved.getResponse().getContentAsString(), "$.id");

        mvc.perform(post("/api/reservations").session(member)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());

        mvc.perform(post("/api/me/reservations/" + reservationId + "/cancel").session(member))
                .andExpect(status().isOk());

        mvc.perform(get("/api/students").session(member)).andExpect(status().isForbidden());
    }

    @Test
    void スタッフは生徒一覧を取得できる() throws Exception {
        MockHttpSession staff = login("staff", "staff1234");
        mvc.perform(get("/api/students").session(staff)).andExpect(status().isOk());
        mvc.perform(get("/api/reservations").session(staff)).andExpect(status().isOk());
    }
}
