package result

type ReconResult struct {
	Success bool   `json:"success"`
	OrderNo string `json:"orderNo"`
	Msg     string `json:"msg"`
}

func Success(orderNo string) *ReconResult {
	return &ReconResult{
		Success: true,
		OrderNo: orderNo,
		Msg:     "Success",
	}
}

func Fail(orderNo, msg string) *ReconResult {
	return &ReconResult{
		Success: false,
		OrderNo: orderNo,
		Msg:     msg,
	}
}

// PageResult 分页结果
type PageResult struct {
	List  interface{} `json:"list"`
	Total int64       `json:"total"`
	Size  int         `json:"size"`
	Page  int         `json:"page"`
}
