package tarce.model;

import java.util.List;

/**
 * Created by zouwansheng on 2017/12/22.
 */

public class StateCountBean {
    /**
     * jsonrpc : 2.0
     * id : null
     * result : {"res_data":[{"state":"waiting_inventory_material","state_count":1},{"state":"waiting_warehouse_inspection","state_count":1}],"res_msg":"","res_code":1}
     */

    private String jsonrpc;
    private Object id;
    private ResultBean result;
    private ErrorBean error;

    public ErrorBean getError() {
        return error;
    }

    public void setError(ErrorBean error) {
        this.error = error;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * res_data : [{"state":"waiting_inventory_material","state_count":1},{"state":"waiting_warehouse_inspection","state_count":1}]
         * res_msg :
         * res_code : 1
         */

        private String res_msg;
        private int res_code;
        private List<ResDataBean> res_data;

        public String getRes_msg() {
            return res_msg;
        }

        public void setRes_msg(String res_msg) {
            this.res_msg = res_msg;
        }

        public int getRes_code() {
            return res_code;
        }

        public void setRes_code(int res_code) {
            this.res_code = res_code;
        }

        public List<ResDataBean> getRes_data() {
            return res_data;
        }

        public void setRes_data(List<ResDataBean> res_data) {
            this.res_data = res_data;
        }

        public static class ResDataBean {
            /**
             * state : waiting_inventory_material
             * state_count : 1
             */

            private String state;
            private int state_count;

            public String getState() {
                return state;
            }

            public void setState(String state) {
                this.state = state;
            }

            public int getState_count() {
                return state_count;
            }

            public void setState_count(int state_count) {
                this.state_count = state_count;
            }
        }
    }
}
