<?php defined('BASEPATH') OR exit('No direct script access allowed');



/*

 * To change this license header, choose License Headers in Project Propertises.

 * To change this template file, choose Tools | Templates

 * and open the template in the editor.

 */



class Json_sender extends CI_Controller {



    private $SLA_DELAY = 3;



    private $STATUS_UNREAD = 0;
    
    private $STATUS_READ = 1;

    private $STATUS_PENDING = 1;

    private $STATUS_REJECTED = 2;
    
    private $STATUS_REJECTED_BY_MANAGER = 4;

    private $STATUS_APPROVED = 3;
    
    private $STATUS_APPROVED_BY_MANAGER = 5;

    private $ROLE_FINANCE = 3;
    private $ROLE_LEGAL = 4;
    private $ROLE_OFFICER = 5;
    private $ROLE_VENDOR = 6;
    private $ROLE_REVIEWER = 7;
    private $Mgr_HC = 8;
    private $Mgr_TSR_VII = 9;
    private $Mgr_Industri_Marine = 10;
    private $Mgr_Retail = 11;
    private $Mgr_QM = 12;
    private $Mgr_Internal_Audit = 13;
    private $Mgr_IT_MOR_VII = 14;
    private $Mgr_Marine_Region_VII = 15;
    private $Mgr_Domgas_Region_VII = 16;
    private $Mgr_Aviation_Region_VII = 17;
    private $Mgr_S_dan_D_Region_VII = 18;
    private $Mgr_HSSE_MOR_VII = 19;
    private $Mgr_Assets_Management_MOR_VII = 20;
    private $Mgr_Medical_Sulawesi = 21;
    private $Staf_HC = 22;
    private $Staf_TSR_VII = 23;
    private $Staf_Industri_Marine = 24;
    private $Staf_Retail = 25;
    private $Staf_QM = 26;
    private $Staf_Internal_Audit = 27;
    private $Staf_IT_MOR_VII = 28;
    private $Staf_Marine_Region_VII = 29;
    private $Staf_Domgas_Region_VII = 30;
    private $Staf_Avigation_Region_VII = 31;
    private $Staf_S_dan_D_Region_VII = 32;
    private $Staf_HSSE_MOR_VII = 33;
    private $Staf_Asset_Management_MOR_VII = 34;
    private $Staf_Medical_Sulawesi = 35;
    private $Reviewer_Vendor = 36;
    private $Mgr_Finance = 37;
    private $Mgr_Legal = 38;
    private $Staf_Finance = 39;
    private $Staf_Legal = 40;
    private $Mgr_Procurement = 41;

    public function __construct() {

        parent::__construct();

        $this->load->model('M_contract');

        $this->load->model('M_user');

        $this->load->model('M_template');

        $this->load->model('M_field');

        $this->load->model('M_pdf');

        $this->load->model('M_api_log');

        $this->load->model('M_officer_contract');

    }

    function cekUser(){
          //$id_contract = $this->input->get('id_contract');

        $host = "localhost";
        $user = "mor7com_digitalcontractv3";
        $password = "mor7com_digitalcontractv3";
        $namaDb = "mor7com_digitalcontractv3";
        $kon = mysqli_connect($host, $user, $password, $namaDb);

        $result = mysqli_query($kon, "SELECT * FROM `user`");

        while ($row = mysqli_fetch_array($result)) {

            echo "Username : ".$row['USERNAME']."___";
            echo "User Role : ".$row['USER_ROLE']."\n";
            
        }

    }
    
    function publishContract(){
        $content = trim(file_get_contents("php://input"));
        $decoded = json_decode($content);
        $id_contract = $decoded->id_contract;
        $publish = $decoded->publish;

        $host = "localhost";
        $user = "mor7com_digitalcontractv3";
        $password = "mor7com_digitalcontractv3";
        $namaDb = "mor7com_digitalcontractv3";
        $kon = mysqli_connect($host, $user, $password, $namaDb);

        $result = mysqli_query($kon, "UPDATE `tr_contract` SET `PUBLISHED` = $publish WHERE `tr_contract`.`CONTRACT_ID` = $id_contract");
        
        if($result){
            echo "Success";
        }
        else{
            echo "Failed";
        }

    }


    function get_contract() {

        // $this->cekUser();

        $content = trim(file_get_contents("php://input"));

        $decoded = json_decode($content);

        $user_id = $decoded->id_user;

        $status = $decoded->status;



        $data_user = $this->M_user->get_by_id($user_id)->row();

        if ($data_user == '') {

            echo json_encode(array("response" => -1));

            exit();

        }

        $role_user = $data_user->USER_ROLE;

        $search_array = array();


        switch ($role_user) {

            case $this->ROLE_REVIEWER:

            if ($status < $this->STATUS_UNREAD) {

                $search_array = array(

                    'reviewer_id' => $user_id,

                    'published' => 1,
                    
                    'active' => 1,

                );

            } else {

                $search_array = array(

                    'reviewer_id' => $user_id,

                    'reviewer_status' => $status,

                    'published' => 1,
                    
                    'active' => 1,

                );

            }
            
            break;

            case $this->ROLE_FINANCE:

            if ($status < $this->STATUS_UNREAD) {

                $search_array = array(

                    'finance_id' => $user_id,

                    'published' => 1,
                    
                    'active' => 1,

                );

            } else {

                $search_array = array(

                    'finance_id' => $user_id,

                    'finance_status' => $status,

                    'published' => 1,
                    
                    'active' => 1,

                );

            }

            break;

            case $this->ROLE_LEGAL:

            if ($status < $this->STATUS_UNREAD) {

                $search_array = array(

                    'legal_id' => $user_id,

                    'published' => 1,
                    
                    'active' => 1,

                );

            } else {

                $search_array = array(

                    'legal_id' => $user_id,

                    'legal_status' => $status,

                    'published' => 1,
                    
                    'active' => 1,

                );

            }

            break;

            case $this->ROLE_VENDOR:

            if ($status < $this->STATUS_UNREAD) {

                $search_array = array(

                    'vendor_id' => $user_id,
                    
                    'officer_certificate' => $this->STATUS_APPROVED,

                    'mgr_finance_status' => $this->STATUS_APPROVED_BY_MANAGER,

                    'mgr_legal_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_hsse_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_reviewer_status' => $this->STATUS_APPROVED_BY_MANAGER,

                    'published' => 1,
                    
                    'active' => 1,

                );

            } else if ($status == 1) {

                $search_array = array(

                    'vendor_id' => $user_id,
                    
                    'officer_certificate' => $this->STATUS_APPROVED,

                    'mgr_finance_status' => $this->STATUS_APPROVED_BY_MANAGER,

                    'mgr_legal_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_hsse_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_reviewer_status' => $this->STATUS_APPROVED_BY_MANAGER,

                    'vendor_certificate' => 5,

                    'published' => 1,
                    
                    'active' => 1,

                );

            } else if ($status == 2) {

                $search_array = array(

                    'vendor_id' => $user_id,
                    
                    'officer_certificate' => $this->STATUS_APPROVED,

                    'mgr_finance_status' => $this->STATUS_APPROVED_BY_MANAGER,

                    'mgr_legal_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_hsse_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_reviewer_status' => $this->STATUS_APPROVED_BY_MANAGER,

                    'vendor_certificate' => 4,

                    'published' => 1,
                    
                    'active' => 1,

                );

            }
            
            else if ($status == 3) {

                $search_array = array(

                    'vendor_id' => $user_id,
                    
                    'officer_certificate' => $this->STATUS_APPROVED,

                    'mgr_finance_status' => $this->STATUS_APPROVED_BY_MANAGER,

                    'mgr_legal_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_hsse_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_reviewer_status' => $this->STATUS_APPROVED_BY_MANAGER,

                    'vendor_certificate' => 5,

                    'published' => 1,
                    
                    'active' => 1,

                );

            } 

            break;

            case $this->ROLE_OFFICER:
            
            if ($status < $this->STATUS_UNREAD){

                $search_array = array(

                    'reviewer_vendor_certificate' => $this->STATUS_APPROVED,
                    
                    'mgr_finance_status' => $this->STATUS_APPROVED_BY_MANAGER,

                    'mgr_legal_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_hsse_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_reviewer_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'officer_id' => $user_id,

                    'published' => 1,
                    
                    'active' => 1,

                );
                
                $this->M_contract->fields = array("CONTRACT_ID", "CONTRACT_TITLE", "CREATED_ON", "LEGAL_STATUS", "MGR_REVIEWER_STATUS", "REVIEWER_STATUS", "REVIEWER_STATUS_2", "FINANCE_STATUS", "VENDOR_SIGNATURE", "VENDOR_CERTIFICATE", "OFFICER_SIGNATURE", "OFFICER_CERTIFICATE", "PDF_PATH", "MGR_LEGAL_STATUS","LEGAL_ID", "MGR_FINANCE_STATUS", "FINANCE_ID", "MGR_HSSE_STATUS", "HSSE_ID", "HSSE_STATUS", "FUNGSI_ID", "REVIEWER_ID", "REVIEWER_ID_2", "PUBLISHED");

                $contract = $this->M_contract->search($search_array)->result();
                
                $this->set_api_log('get_contract', $user_id, 1);
                
                echo json_encode(array("response" => $contract));

            } else if ($status == $this->STATUS_APPROVED) {

                $search_array = array(

                    'reviewer_vendor_certificate' => $this->STATUS_APPROVED,
                    
                    'officer_certificate' => $this->STATUS_APPROVED,
                    
                    'mgr_finance_status' => $this->STATUS_APPROVED_BY_MANAGER,

                    'mgr_legal_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_hsse_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_reviewer_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'officer_id' => $user_id,

                    'published' => 1,
                    
                    'active' => 1,

                );
                
                $this->M_contract->fields = array("CONTRACT_ID", "CONTRACT_TITLE", "CREATED_ON", "LEGAL_STATUS", "MGR_REVIEWER_STATUS", "REVIEWER_STATUS", "REVIEWER_STATUS_2", "FINANCE_STATUS", "VENDOR_SIGNATURE", "VENDOR_CERTIFICATE", "OFFICER_SIGNATURE", "OFFICER_CERTIFICATE", "PDF_PATH", "MGR_LEGAL_STATUS","LEGAL_ID", "MGR_FINANCE_STATUS", "FINANCE_ID", "MGR_HSSE_STATUS", "HSSE_ID", "HSSE_STATUS", "FUNGSI_ID", "REVIEWER_ID", "REVIEWER_ID_2", "PUBLISHED");

                $contract = $this->M_contract->search($search_array)->result();
                
                $this->set_api_log('get_contract', $user_id, 1);
                
                echo json_encode(array("response" => $contract));

            }
            else {

                $search_array = array(

                    'reviewer_vendor_certificate' => $this->STATUS_APPROVED,
                    
                    'officer_certificate' => $status,
                    
                    'mgr_finance_status' => $this->STATUS_APPROVED_BY_MANAGER,

                    'mgr_legal_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_hsse_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_reviewer_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'officer_id' => $user_id,

                    'published' => 1,
                    
                    'active' => 1,

                );

                $search_array_vendor = array(

                    'vendor_certificate' => $this->STATUS_APPROVED,
                    
                    'mgr_finance_status' => $this->STATUS_APPROVED_BY_MANAGER,

                    'mgr_legal_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_hsse_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'mgr_reviewer_status' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'officer_id' => $user_id,

                    'published' => 1,
                    
                    'active' => 1,

                );

                $this->M_contract->fields = array("CONTRACT_ID", "CONTRACT_TITLE", "CREATED_ON", "LEGAL_STATUS", "MGR_REVIEWER_STATUS", "REVIEWER_STATUS", "REVIEWER_STATUS_2", "FINANCE_STATUS", "VENDOR_SIGNATURE", "VENDOR_CERTIFICATE", "OFFICER_SIGNATURE", "OFFICER_CERTIFICATE", "PDF_PATH", "MGR_LEGAL_STATUS","LEGAL_ID", "MGR_FINANCE_STATUS", "FINANCE_ID", "MGR_HSSE_STATUS", "HSSE_ID", "HSSE_STATUS", "FUNGSI_ID", "REVIEWER_ID", "REVIEWER_ID_2", "PUBLISHED");

                $contract = $this->M_contract->search($search_array)->result();
                
                $contract_vendor = $this->M_contract->search($search_array_vendor)->result();
                
                for ($a = 0; $a < sizeof($contract_vendor); $a++){
                    
                    array_push($contract, $contract_vendor[$a]);
                    
                }
                
                $this->set_api_log('get_contract', $user_id, 1);
                
                echo json_encode(array("response" => $contract));
            }
            
            exit();
            
            break;
            
            case $this->Mgr_Procurement:
            
            if ($status <= $this->STATUS_UNREAD){
                
                $search_array = array(

                    'published' => 0,
                    
                    'active' => 1,

                );  
                
            }
            
            else if ($status <= $this->STATUS_READ){
                
                $search_array = array(

                    'published' => 1,
                    
                    'active' => 1,

                );  
                
            }
            
            else{
                
                $search_array = array(
                    
                    'published' => 0,
                    
                    'active' => 1
                    
                );
                
            }

            break;
            
            case $this->Mgr_Finance:

            if ($status < $this->STATUS_UNREAD) {

                $search_array = array(

                    'published' => 1,
                    
                    'active' => 1

                );

            }
            
            else if ($status == $this->STATUS_APPROVED){
                $search_array = array(

                    'MGR_FINANCE_STATUS' => 5,

                    'published' => 1,
                    
                    'active' => 1

                );
            }
            
            else{
                $search_array = array(

                    'MGR_FINANCE_STATUS' => $status,

                    'published' => 1,
                    
                    'active' => 1

                );
            }
            
            break;
            
            case $this->Mgr_Legal:
                
            if ($status < $this->STATUS_UNREAD) {

                $search_array = array(

                    'published' => 1,
                    
                    'active' => 1

                );

            }
            
            else if ($status == $this->STATUS_APPROVED){
                $search_array = array(

                    'MGR_LEGAL_STATUS' => 5,

                    'published' => 1,
                    
                    'active' => 1

                );
            }
            
            else{
                
                $search_array = array(

                    'MGR_LEGAL_STATUS' => $status,

                    'published' => 1,
                    
                    'active' => 1

                );
            }
            
            break;
            
            case $this->Mgr_HSSE_MOR_VII:

            if ($status < $this->STATUS_UNREAD) {

                $search_array = array(

                    'published' => 1,
                    
                    'active' => 1

                );

            }
            
            else if ($status == $this->STATUS_APPROVED){
                
                $search_array = array(

                    'MGR_HSSE_STATUS' => 5,

                    'published' => 1,
                    
                    'active' => 1

                );
            }
            
            else{
                $search_array = array(

                    'MGR_HSSE_STATUS' => $status,

                    'published' => 1,
                    
                    'active' => 1

                );
            }

            break;
            
            case $this->Staf_Finance:
                
            if ($status < $this->STATUS_UNREAD) {

                $search_array = array(
                    
                    'finance_id' => $user_id,

                    'published' => 1,
                    
                    'active' => 1

                );

            }
            else{
                
                $search_array = array(
                    
                    'finance_id' => $user_id,

                    'finance_status' => $status,

                    'published' => 1,
                    
                    'active' => 1

                );
            }

            break;
            
            case $this->Staf_Legal:
            
            if ($status < $this->STATUS_UNREAD) {

                $search_array = array(
                    
                    'legal_id' => $user_id,

                    'published' => 1,
                    
                    'active' => 1

                );

            }
            else{
                $search_array = array(
                    
                    'legal_id' => $user_id,

                    'legal_status' => $status,

                    'published' => 1,
                    
                    'active' => 1

                );
            }

            break;
            
            case $this->Staf_HSSE_MOR_VII:
            
            if ($status < $this->STATUS_UNREAD) {

                $search_array = array(
                    
                    'hsse_id' => $user_id,

                    'published' => 1,
                    
                    'active' => 1

                );

            }
            else{
                $search_array = array(
                    
                    'hsse_id' => $user_id,

                    'hsse_status' => $status,

                    'published' => 1,
                    
                    'active' => 1

                );
            }

            break;
            case $this->Reviewer_Vendor:
            
            if ($status < $this->STATUS_UNREAD) {

                $search_array = array(
                    
                    'MGR_HSSE_STATUS' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'MGR_LEGAL_STATUS' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'MGR_FINANCE_STATUS' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'MGR_REVIEWER_STATUS' => $this->STATUS_APPROVED_BY_MANAGER,

                    'published' => 1,
                    
                    'active' => 1

                );

            }
            else{
                
                $search_array = array(
                    
                    'MGR_HSSE_STATUS' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'MGR_LEGAL_STATUS' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'MGR_FINANCE_STATUS' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'MGR_REVIEWER_STATUS' => $this->STATUS_APPROVED_BY_MANAGER,
                    
                    'REVIEWER_VENDOR_CERTIFICATE' => $status,

                    'published' => 1,
                    
                    'active' => 1

                );
            }
            
            $this->M_contract->fields = array("CONTRACT_ID", "CONTRACT_TITLE", "CREATED_ON", "LEGAL_STATUS", "MGR_REVIEWER_STATUS", "REVIEWER_STATUS", "REVIEWER_STATUS_2", "FINANCE_STATUS", "VENDOR_ID", "VENDOR_SIGNATURE", "REVIEWER_VENDOR_CERTIFICATE", "VENDOR_CERTIFICATE", "OFFICER_SIGNATURE", "OFFICER_CERTIFICATE", "PDF_PATH", "MGR_LEGAL_STATUS","LEGAL_ID", "MGR_FINANCE_STATUS", "FINANCE_ID", "MGR_HSSE_STATUS", "HSSE_ID", "HSSE_STATUS", "FUNGSI_ID", "REVIEWER_ID", "REVIEWER_ID_2", "PUBLISHED");

            $contract = $this->M_contract->search($search_array)->result();
            
            $this->set_api_log('get_contract', $user_id, 1);
            
            $data_vendor = array();
            
            $data_hasil_contract = array();
            
            for ($i = 0; $i < sizeof($contract); $i++){

                $search_vendor = array(

                    'USER_ID' => $user_id,
                    
                    'COMPANY_ID' => $contract[$i]->VENDOR_ID
                );
                
                $this->M_user->fields = array("COMPANY_ID");

                $dataVendor = $this->M_user->search($search_vendor)->result();
                
                if (sizeOf($dataVendor) != 0){
                    
                    array_push($data_vendor, $dataVendor[0]->COMPANY_ID);
                    
                }

            }
            
            for ($i = 0; $i < sizeof($contract); $i++){
                
                if (sizeOf($dataVendor) != 0){

                    if ($data_user->COMPANY_ID == $data_vendor[$i]){

                        array_push($data_hasil_contract, $contract[$i]);
                    
                    }
                }
            }
            
            // echo $data_vendor[0];

            echo json_encode(array("response" => $data_hasil_contract));

            exit();
            
            break;
            
            default:
                
            if (($role_user >= 8) && ($role_user <= 18) || $role_user == 20 || $role_user == 21 ){
                
                if ($status < $this->STATUS_UNREAD) {

                    $search_array = array(

                        'published' => 1,
                        
                        'active' => 1

                    );

                }
            
                else if ($status == $this->STATUS_APPROVED){
                    
                    $search_array = array(

                        'MGR_REVIEWER_STATUS' => 5,

                        'published' => 1,
                        
                        'active' => 1

                    );
                }
            
                else{
                    
                    $search_array = array(
                        
                        'MGR_REVIEWER_STATUS' => $status,

                        'published' => 1,
                        
                        'active' => 1

                    );
                }
                
            }
            else if (($role_user >= 22) && ($role_user <= 32) || $role_user == 34 || $role_user == 35){
                
                if ($status < $this->STATUS_UNREAD) {

                    $search_array = array(
                    
                        'reviewer_id' => $user_id,

                        'published' => 1,
                        
                        'active' => 1

                    );

                }
                else{
                    $search_array = array(
                    
                        'reviewer_id' => $user_id,

                        'reviewer_status' => $status,

                        'published' => 1,
                        
                        'active' => 1

                    );
                }
                
                if ($status < $this->STATUS_UNREAD) {

                    $search_array_2 = array(
                    
                        'reviewer_id_2' => $user_id,

                        'published' => 1,
                        
                        'active' => 1

                    );

                }
                else{
                    $search_array_2 = array(
                    
                        'reviewer_id_2' => $user_id,

                        'reviewer_status_2' => $status,

                        'published' => 1,
                        
                        'active' => 1

                    );
                }
                
                $this->M_contract->fields = array("CONTRACT_ID", "CONTRACT_TITLE", "CREATED_ON", "LEGAL_STATUS", "MGR_REVIEWER_STATUS", "REVIEWER_STATUS", "REVIEWER_STATUS_2", "FINANCE_STATUS", "VENDOR_SIGNATURE", "VENDOR_CERTIFICATE", "OFFICER_SIGNATURE", "OFFICER_CERTIFICATE", "PDF_PATH", "MGR_LEGAL_STATUS","LEGAL_ID", "MGR_FINANCE_STATUS", "FINANCE_ID", "MGR_HSSE_STATUS", "HSSE_ID", "HSSE_STATUS", "FUNGSI_ID", "REVIEWER_ID", "REVIEWER_ID_2", "PUBLISHED");

                $contract = $this->M_contract->search($search_array)->result();
                
                $contract_vendor = $this->M_contract->search($search_array_2)->result();
                
                for ($a = 0; $a < sizeof($contract_vendor); $a++){

                    array_push($contract, $contract_vendor[$a]);
                }
                
                $this->set_api_log('get_contract', $user_id, 1);
                
                echo json_encode(array("response" => $contract));

                exit();
            }
            else{
                echo json_encode(array("response" => -1));

                exit();
            }
        }

        $this->M_contract->fields = array("CONTRACT_ID", "CONTRACT_TITLE", "CREATED_ON", "LEGAL_STATUS", "MGR_REVIEWER_STATUS", "REVIEWER_STATUS", "REVIEWER_STATUS_2", "FINANCE_STATUS", "VENDOR_SIGNATURE", "VENDOR_CERTIFICATE", "OFFICER_SIGNATURE", "OFFICER_CERTIFICATE", "PDF_PATH", "MGR_LEGAL_STATUS","LEGAL_ID", "MGR_FINANCE_STATUS", "FINANCE_ID", "MGR_HSSE_STATUS", "HSSE_ID", "HSSE_STATUS", "FUNGSI_ID", "REVIEWER_ID", "REVIEWER_ID_2", "PUBLISHED");

        $contract = $this->M_contract->search($search_array)->result();

        $this->set_api_log('get_contract', $user_id, 1);

        echo json_encode(array("response" => $contract));

    }



    function get_document() {

        $contract_id = $this->input->get('id');

        $user_id = $this->input->get('id_user');

        if ($contract_id == '') {

            $content = trim(file_get_contents("php://input"));

            $decoded = json_decode($content);

            $contract_id = $decoded->id_contract;

            $user_id = $decoded->id_user;

        }



        $contract = $this->M_contract->search(array('contract_id' => $contract_id))->row();



        $this->M_pdf->fields = array("NAME", "SIZE", "TOKEN", "PATH");

        $search_array = array( 'CONTRACT_ID' => $contract_id );

        $pdf = $this->M_pdf->search($search_array)->result();

        // $response = [$contract, $pdf];

        $this->set_api_log('get_document', $user_id, 1);

        // echo json_encode(array("response" => $response));
        echo json_encode(array("response" => $contract, "extra" => $pdf));

    }



    function set_imei() {

        $content = trim(file_get_contents("php://input"));

        $decoded = json_decode($content);

        $user_id = $decoded->id_user;

        $imei = $decoded->imei;



        $data = array('imei' => $imei);



        $this->M_user->set($user_id, $data);

        $this->set_api_log('set_imei', $user_id, 1);

        echo json_encode(array("response" => $this->db->affected_rows()));

    }



    function set_token(){

        $content = trim(file_get_contents("php://input"));

        $decoded = json_decode($content);

        $user_id = $decoded->id_user;

        $token = $decoded->token;



        $data = array('token_data_1' => $token);



        $this->M_user->set($user_id, $data);

        $this->set_api_log('set_token', $user_id, 1);

        echo json_encode(array("response" => $this->db->affected_rows()));

    }



    function set_status() {

        $content = trim(file_get_contents("php://input"));

        $decoded = json_decode($content);

        $contract_id = $decoded->id_contract;

        $user_id = $decoded->id_user;

        $param1 = $decoded->user_status;

        $param2 = $decoded->note;

        $field_note = '';

        $data_user = $this->M_user->get_by_id($user_id)->row();

        $data_contract = $this->M_contract->get_by_id($contract_id)->row();

        if (!$data_user) {

            $this->set_api_log('set_status', '-1', 0);

            echo json_encode(array("response" => -1));

            exit();

        }

        $role = $data_user->USER_ROLE;

        $date = date('Y-m-d H:i:s');

        switch ($role) {

            case $this->ROLE_REVIEWER:


            $data = array(

                "reviewer_status" => $param1,

                "reviewer_note" => $param2,

            );

            break;

            case $this->ROLE_FINANCE:

            if ($data_contract->FINANCE_STATUS < $this->STATUS_APPROVED) {

                $data = array(

                    "finance_status" => $param1,

                    "finance_note" => $param2,

                );

                if ($param1 >= $this->STATUS_APPROVED) {

                    $data['finance_datetime'] = $date;

                    $data['finance_sla'] = $this->sla_duration($data['finance_datetime'], $data_contract->PUBLISHED_DATETIME);

                }

                if ($data_contract->LEGAL_STATUS >= $this->STATUS_APPROVED && $param1 >= $this->STATUS_APPROVED){

                    $data_target = $this->M_user->get_by_id($data_contract->VENDOR_ID)->row();

                    $this->fcm($data_contract->CONTRACT_TITLE, $data_target->TOKEN_DATA_1);

                }

            }

            break;

            case $this->ROLE_LEGAL:

            if ($data_contract->LEGAL_STATUS < $this->STATUS_APPROVED) {

                $data = array(

                    "legal_status" => $param1,

                    "legal_note" => $param2,

                );

                if ($param1 >= $this->STATUS_APPROVED) {

                    $data['legal_datetime'] = $date;

                    $data['legal_sla'] = $this->sla_duration($data['legal_datetime'], $data_contract->PUBLISHED_DATETIME);

                }

                if ($data_contract->FINANCE_STATUS >= $this->STATUS_APPROVED && $param1 >= $this->STATUS_APPROVED){

                    $data_target = $this->M_user->get_by_id($data_contract->VENDOR_ID)->row();

                    $this->fcm($data_contract->CONTRACT_TITLE, $data_user->TOKEN_DATA_1);

                }

            }

            break;

            case $this->ROLE_VENDOR:

                $data = array(

                    "vendor_certificate" => $param1,

                    "vendor_signature" => $param2,

                );

            break;

            case $this->ROLE_OFFICER:

            $data = array(

                "officer_certificate" => $param1,

                "officer_signature" => $param2,

            );

            break;
            case $this->Mgr_Legal:

            if ($data_contract->MGR_LEGAL_STATUS < $this->STATUS_APPROVED) {

                $data = array(

                    "MGR_LEGAL_STATUS" => $param1,

                );

            } else if ($data_contract->MGR_LEGAL_STATUS == $this->STATUS_APPROVED) {
                
                $param2 = "";
                
                
                if($param1 == 4 ){
                    
                        $param2 = $data_contract->LEGAL_NOTE."\n".$date." [Manager] Accept = "." = Approved \n";
                    
                        $data_manager = $this->M_user->get_id($data_contract->LEGAL_ID)->row();
                    
                        $data_staff = $this->M_user->get_id($user_id)->row();
                    
                        $data_body1['title'] = "Digital Contract";
                    
                        $data_body1['message'] = 'Kontrak : '.$data_contract->CONTRACT_TITLE.' perlu di review ulang';
                    
                        $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                        $this->notification($token_vendor, $data_body1);
                    
                    }
                
                if($param1 == 5 && $data_contract->MGR_FINANCE_STATUS == 5 && $data_contract->MGR_HSSE_STATUS == 5 && $data_contract->MGR_REVIEWER_STATUS == 5){
                    
                        $param2 = $data_contract->LEGAL_NOTE."\n".$date." [Manager] Reject = "." = ".$param2."\n";
                    
                        $data_manager = $this->M_user->get_id('78')->row();
                    
                        $data_staff = $this->M_user->get_id($user_id)->row();
                    
                        $data_body1['title'] = "Digital Contract";
                    
                        $data_body1['message'] = 'Ada Kontrak baru yang perlu anda review';
                    
                        $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                        $this->notification($token_vendor, $data_body1);
                    
                    }
                    
                $data = array(

                    "MGR_LEGAL_STATUS" => $param1,
                    
                    "LEGAL_NOTE" => $param2

                );
            }

            break;
            case $this->Mgr_Finance:

            if ($data_contract->MGR_FINANCE_STATUS < $this->STATUS_APPROVED) {

                $data = array(

                    "MGR_FINANCE_STATUS" => $param1,

                );

            } else if ($data_contract->MGR_FINANCE_STATUS == $this->STATUS_APPROVED) {
                
                $param2 = $data_contract->FINANCE_NOTE."Manager = ".$date." = ".$param2."\n";
                
                $data = array(

                    "MGR_FINANCE_STATUS" => $param1,
                    
                    "FINANCE_NOTE" => $param2

                );
                
                if($param1 == 5 && $data_contract->MGR_LEGAL_STATUS == 5 && $data_contract->MGR_HSSE_STATUS == 5 && $data_contract->MGR_REVIEWER_STATUS == 5){
                    
                        $data_manager = $this->M_user->get_id('78')->row();
                    
                        $data_staff = $this->M_user->get_id($user_id)->row();
                    
                        $data_body1['title'] = "Digital Contract";
                    
                        $data_body1['message'] = 'Ada Kontrak baru yang perlu anda review';
                    
                        $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                        $this->notification($token_vendor, $data_body1);
                    
                    }
                    
                if($param1 == 4 ){
                    
                        $data_manager = $this->M_user->get_id($data_contract->FINANCE_ID)->row();
                    
                        $data_staff = $this->M_user->get_id($user_id)->row();
                    
                        $data_body1['title'] = "Digital Contract";
                    
                        $data_body1['message'] = 'Kontrak : '.$data_contract->CONTRACT_TITLE.' perlu di review ulang';
                    
                        $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                        $this->notification($token_vendor, $data_body1);
                    
                    }
            }

            break;
            case $this->Mgr_HSSE_MOR_VII:

            if ($data_contract->MGR_HSSE_STATUS < $this->STATUS_APPROVED) {

                $data = array(

                    "MGR_HSSE_STATUS" => $param1,

                );

            } else if ($data_contract->MGR_HSSE_STATUS == $this->STATUS_APPROVED) {
                
                $param2 = $data_contract->HSSE_NOTE."Manager = ".$date." = ".$param2."\n";
                
                $data = array(

                    "MGR_HSSE_STATUS" => $param1,
                    
                    "HSSE_NOTE" => $param2

                );
                
                if($param1 == 4 ){
                    
                        $data_manager = $this->M_user->get_id($data_contract->HSSE_ID)->row();
                    
                        $data_staff = $this->M_user->get_id($user_id)->row();
                    
                        $data_body1['title'] = "Digital Contract";
                    
                        $data_body1['message'] = 'Kontrak : '.$data_contract->CONTRACT_TITLE.' perlu di review ulang';
                    
                        $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                        $this->notification($token_vendor, $data_body1);
                    
                    }
                
                if($param1 == 5 && $data_contract->MGR_FINANCE_STATUS == 5 && $data_contract->MGR_LEGAL_STATUS == 5 && $data_contract->MGR_REVIEWER_STATUS == 5){
                    
                        $data_manager = $this->M_user->get_id('78')->row();
                    
                        $data_staff = $this->M_user->get_id($user_id)->row();
                    
                        $data_body1['title'] = "Digital Contract";
                    
                        $data_body1['message'] = 'Ada Kontrak baru yang perlu anda review';
                    
                        $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                        $this->notification($token_vendor, $data_body1);
                    
                    }
            }

            break;
            case $this->Staf_Finance:

            if ($data_contract->FINANCE_STATUS < $this->STATUS_APPROVED) {

                $param2 = $data_contract->FINANCE_NOTE."Staff = ".$date." = ".$param2."\n";

                $data = array(

                    "FINANCE_STATUS" => $param1,

                    "FINANCE_NOTE" => $param2

                );
                
                if($param1 == 3){
                    
                    $data_manager = $this->M_user->get_id('72')->row();
                    
                    $data_staff = $this->M_user->get_id($user_id)->row();
                    
                    $data_body1['title'] = "Digital Contract";
                    
                    $data_body1['message'] = 'Kontrak '.$data_contract->CONTRACT_TITLE.' telah selesai di review oleh Staff : '.$data_staff->NAME;
                    
                    $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                    $this->notification($token_vendor, $data_body1);
                    
                }

            }
            
            else if ($data_contract->FINANCE_STATUS == $this->STATUS_APPROVED && $data_contract->MGR_FINANCE_STATUS == $this->STATUS_REJECTED_BY_MANAGER) {
                
                $param2 = $data_contract->FINANCE_NOTE."Staff = ".$date." = ".$param2."\n";

                $data = array(

                    "MGR_FINANCE_STATUS" => $this->STATUS_APPROVED,

                    "FINANCE_NOTE" => $param2

                );
                
                if($param1 == 3){
                    
                    $data_manager = $this->M_user->get_id('72')->row();
                    
                    $data_staff = $this->M_user->get_id($user_id)->row();
                    
                    $data_body1['title'] = "Digital Contract";
                    
                    $data_body1['message'] = 'Kontrak '.$data_contract->CONTRACT_TITLE.' telah selesai di review oleh Staff : '.$data_staff->NAME;
                    
                    $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                    $this->notification($token_vendor, $data_body1);
                    
                }
                
            }

            break;
            case $this->Staf_Legal:

            if ($data_contract->LEGAL_STATUS < $this->STATUS_APPROVED) {

                $param2 = $data_contract->LEGAL_NOTE."Staff = ".$date." = ".$param2."\n";

                $data = array(

                    "LEGAL_STATUS" => $param1,

                    "LEGAL_NOTE" => $param2

                );
                
                if($param1 == 3){
                    
                    $data_manager = $this->M_user->get_id('74')->row();
                    
                    $data_staff = $this->M_user->get_id($user_id)->row();
                    
                    $data_body1['title'] = "Digital Contract";
                    
                    $data_body1['message'] = 'Kontrak '.$data_contract->CONTRACT_TITLE.' telah selesai di review oleh Staff : '.$data_staff->NAME;
                    
                    $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                    $this->notification($token_vendor, $data_body1);
                    
                }
                
            }
            
            else if ($data_contract->LEGAL_STATUS == $this->STATUS_APPROVED && $data_contract->MGR_LEGAL_STATUS == $this->STATUS_REJECTED_BY_MANAGER) {
                
                $param2 = $data_contract->LEGAL_NOTE."Staff = ".$date." = ".$param2."\n";

                $data = array(

                    "MGR_LEGAL_STATUS" => $this->STATUS_APPROVED,

                    "LEGAL_NOTE" => $param2

                );
                
                if($param1 == 3){
                    
                    $data_manager = $this->M_user->get_id('74')->row();
                    
                    $data_staff = $this->M_user->get_id($user_id)->row();
                    
                    $data_body1['title'] = "Digital Contract";
                    
                    $data_body1['message'] = 'Kontrak '.$data_contract->CONTRACT_TITLE.' telah selesai di review oleh Staff : '.$data_staff->NAME;
                    
                    $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                    $this->notification($token_vendor, $data_body1);
                    
                }
                
            }

            break;
            case $this->Staf_HSSE_MOR_VII:

            if ($data_contract->HSSE_STATUS < $this->STATUS_APPROVED) {

                $param2 = $data_contract->HSSE_NOTE."Staff = ".$date." = ".$param2."\n";

                $data = array(

                    "HSSE_STATUS" => $param1,

                    "HSSE_NOTE" => $param2

                );
                
                if($param1 == 3){
                    
                    $data_manager = $this->M_user->get_id('76')->row();
                    
                    $data_staff = $this->M_user->get_id($user_id)->row();
                    
                    $data_body1['title'] = "Digital Contract";
                    
                    $data_body1['message'] = 'Kontrak '.$data_contract->CONTRACT_TITLE.' telah selesai di review oleh Staff : '.$data_staff->NAME;
                    
                    $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                    $this->notification($token_vendor, $data_body1);
                    
                }

            }
            
            else if ($data_contract->HSSE_STATUS == $this->STATUS_APPROVED && $data_contract->MGR_HSSE_STATUS == $this->STATUS_REJECTED_BY_MANAGER) {
                
                $param2 = $data_contract->HSSE_NOTE."Staff = ".$date." = ".$param2."\n";

                $data = array(

                    "MGR_HSSE_STATUS" => $this->STATUS_APPROVED,

                    "HSSE_NOTE" => $param2

                );
                
                if($param1 == 3){
                    
                    $data_manager = $this->M_user->get_id('76')->row();
                    
                    $data_staff = $this->M_user->get_id($user_id)->row();
                    
                    $data_body1['title'] = "Digital Contract";
                    
                    $data_body1['message'] = 'Kontrak '.$data_contract->CONTRACT_TITLE.' telah selesai di review oleh Staff : '.$data_staff->NAME;
                    
                    $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                    $this->notification($token_vendor, $data_body1);
                    
                }
                
            }

            break;
            case $this->Reviewer_Vendor:

            if ($data_contract->REVIEWER_VENDOR_CERTIFICATE < $this->STATUS_APPROVED) {

                $data = array(

                    "REVIEWER_VENDOR_CERTIFICATE" => $param1

                );
                
                if($param1 == 3){
                    
                        $data_manager = $this->M_user->get_id($data_contract->OFFICER_ID)->row();
                    
                        $data_body1['title'] = "Digital Contract";
                    
                        $data_body1['message'] = 'Yth. Officer Ada Kontrak baru yang perlu anda tanda tangani';
                    
                        $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                        $this->notification($token_vendor, $data_body1);
                    
                    }

            }

            break;
            default :
            if (($role >= 22) && ($role <= 32) || ($role == 34) || ($role == 35)){
                
                if ($data_contract->REVIEWER_ID == $user_id){
                    
                    if ($data_contract->REVIEWER_STATUS < $this->STATUS_APPROVED) {

                        $param2 = $data_contract->REVIEWER_NOTE."Staff = ".$date." = ".$param2."\n";

                        $data = array(

                            "REVIEWER_STATUS" => $param1,

                            "REVIEWER_NOTE" => $param2

                        );
                        
                        if($param1 == 3){
                            
                            $host = "localhost";
                            $user = "mor7com_digitalcontractv3";
                            $password = "mor7com_digitalcontractv3";
                            $namaDb = "mor7com_digitalcontractv3";
                            $kon = mysqli_connect($host, $user, $password, $namaDb);

                            $result = mysqli_query($kon, "SELECT * FROM `user` WHERE `USER_ROLE` = '$data_contract->FUNGSI_ID'");

                            if ($result){
                                while ($row = mysqli_fetch_array($result)) {
                    
                                    $data_manager = $this->M_user->get_id($row['USER_ID'])->row();
                    
                                    $data_staff = $this->M_user->get_id($user_id)->row();
                    
                                    $data_body1['title'] = "Digital Contract";
                    
                                    $data_body1['message'] = 'Kontrak '.$data_contract->CONTRACT_TITLE.' telah selesai di review oleh Staff : '.$data_staff->NAME;
                            
                                    $token_vendor = $data_manager->TOKEN_DATA_1;
                            
                                    $this->notification($token_vendor, $data_body1);
                        
                                }
                            }
                            else{
                                echo "Error";
                            }
                    
                        }

                    }
            
                    else if ($data_contract->REVIEWER_STATUS == $this->STATUS_APPROVED && $data_contract->MGR_REVIEWER_STATUS == $this->STATUS_REJECTED_BY_MANAGER) {
                
                        $param2 = $data_contract->REVIEWER_NOTE."Staff = ".$date." = ".$param2."\n";

                            $data = array(

                                "MGR_REVIEWER_STATUS" => $this->STATUS_APPROVED,

                                "REVIEWER_NOTE" => $param2

                            );
                
                    }
                    
                } else{
                    
                    if ($data_contract->REVIEWER_STATUS_2 < $this->STATUS_APPROVED) {

                        $param2 = $data_contract->REVIEWER_NOTE_2."Staff = ".$date." = ".$param2."\n";

                        $data = array(

                            "REVIEWER_STATUS_2" => $param1,

                            "REVIEWER_NOTE_2" => $param2

                        );
                        
                        if($param1 == 3){
                            
                            $host = "localhost";
                            $user = "mor7com_digitalcontractv3";
                            $password = "mor7com_digitalcontractv3";
                            $namaDb = "mor7com_digitalcontractv3";
                            $kon = mysqli_connect($host, $user, $password, $namaDb);

                            $result = mysqli_query($kon, "SELECT * FROM `user` WHERE `USER_ROLE` = '$data_contract->FUNGSI_ID'");

                            if ($result){
                                while ($row = mysqli_fetch_array($result)) {
                    
                                    $data_manager = $this->M_user->get_id($row['USER_ID'])->row();
                    
                                    $data_staff = $this->M_user->get_id($user_id)->row();
                    
                                    $data_body1['title'] = "Digital Contract";
                    
                                    $data_body1['message'] = 'Kontrak '.$data_contract->CONTRACT_TITLE.' telah selesai di review oleh Staff : '.$data_staff->NAME;
                            
                                    $token_vendor = $data_manager->TOKEN_DATA_1;
                            
                                    $this->notification($token_vendor, $data_body1);
                        
                                }
                            }
                            else{
                                echo "Error";
                            }
                    
                        }

                    }
            
                    else if ($data_contract->REVIEWER_STATUS_2 == $this->STATUS_APPROVED && $data_contract->MGR_REVIEWER_STATUS == $this->STATUS_REJECTED_BY_MANAGER) {
                
                        $param2 = $data_contract->REVIEWER_NOTE_2."Staff = ".$date." = ".$param2."\n";

                            $data = array(

                                "MGR_REVIEWER_STATUS" => $this->STATUS_APPROVED,

                                "REVIEWER_NOTE_2" => $param2

                            );
                
                    }
                    
                }

            }
            
            else if (($role >= 8) && ($role <= 18) || ($role == 20) || ($role == 21)){
                
                if ($data_contract->MGR_REVIEWER_STATUS < $this->STATUS_APPROVED) {

                    $data = array(

                        "MGR_REVIEWER_STATUS" => $param1,

                    );

                } else if ($data_contract->MGR_REVIEWER_STATUS == $this->STATUS_APPROVED) {
                
                    $param2 = $data_contract->REVIEWER_NOTE."Manager = ".$date." = ".$param2."\n";
                
                    $data = array(

                        "MGR_REVIEWER_STATUS" => $param1,
                    
                        "REVIEWER_NOTE" => $param2

                    );
                    
                    if($param1 == 4 ){
                    
                        $data_manager = $this->M_user->get_id($data_contract->REVIEWER_ID)->row();
                        
                        $data_manager_2 = $this->M_user->get_id($data_contract->REVIEWER_ID_2)->row();
                    
                        $data_staff = $this->M_user->get_id($user_id)->row();
                    
                        $data_body1['title'] = "Digital Contract";
                    
                        $data_body1['message'] = 'Kontrak : '.$data_contract->CONTRACT_TITLE.' perlu di review ulang';
                    
                        $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                        $this->notification($token_vendor, $data_body1);
                        
                        $token_vendor_2 = $data_manager_2->TOKEN_DATA_1;
                    
                        $this->notification($token_vendor_2, $data_body1);
                    
                    }
                    
                    if($param1 == 5 && $data_contract->MGR_FINANCE_STATUS == 5 && $data_contract->MGR_HSSE_STATUS == 5 && $data_contract->MGR_LEGAL_STATUS == 5){
                    
                        $data_manager = $this->M_user->get_id('78')->row();
                    
                        $data_staff = $this->M_user->get_id($user_id)->row();
                    
                        $data_body1['title'] = "Digital Contract";
                    
                        $data_body1['message'] = 'Ada Kontrak baru yang perlu anda review';
                    
                        $token_vendor = $data_manager->TOKEN_DATA_1;
                    
                        $this->notification($token_vendor, $data_body1);
                    
                    }
                    
                }
                
            }
            
            break;

        }



        $this->M_contract->set($contract_id, $data);

        $this->set_api_log('set_status', $user_id, 1);

        echo json_encode(array("response" => $this->db->affected_rows()));

    }

    function notification($token, array $data = []){
        $url = 'https://fcm.googleapis.com/fcm/send';

        $body = array();

        is_array($token)
        ? $body['registration_ids'] = $token
        : $body['to'] = $token;

        empty($data) ?: $body['data'] = $data;

        $headers = [
            'Authorization: key = AAAA3kAPFi4:APA91bHvOSovLEQ9LF3M6KmuvDSdh0zQWOF4Sm-N1bq2FfzgkZ-3KUip6DeTvI9LWJS44Ugdyu9cN0Q6Jc9wnC_2N5HyNs6-WHmgamebeBzRTU41rR7FciBQ6_9rITtZhHKa4hCa0pUh',
            'Content-Type: application/json',
            'Cache-Control: no-cache'
        ];

        $options = [
            CURLOPT_URL => $url,
            CURLOPT_HTTPHEADER => $headers,
            CURLOPT_POSTFIELDS => json_encode($body),
            CURLOPT_POST => 1,
        ];

        $ch = curl_init();
        curl_setopt_array($ch, $options);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        // $response = ;
        curl_exec($ch);
        curl_close($ch);
    }

    function getLogContract(){
        $content = trim(file_get_contents("php://input"));
        $decoded = json_decode($content);   
        $id_user = $decoded->id_user;
        $id_contract = $decoded->id_contract;

        $host = "localhost";
        $user = "mor7com_digitalcontractv3";
        $password = "mor7com_digitalcontractv3";
        $namaDb = "mor7com_digitalcontractv3";
        $kon = mysqli_connect($host, $user, $password, $namaDb);
        
        $id_request = "";

        switch ($role) {
            case $this->ROLE_VENDOR:

            break;

            case $this->Mgr_Legal:

                $id_request = "LEGAL_STATUS";

            break;
            case $this->Mgr_Finance:

                $id_request = "FINANCE_STATUS";

            break;
            case $this->Mgr_HSSE_MOR_VII:

                $id_request = "HSSE_STATUS";

            break;
            case $this->Staf_Finance:
                            
                $id_request = "FINANCE_STATUS";

            break;
            case $this->Staf_Legal:
           
                $id_request = "LEGAL_STATUS";

            break;
            case $this->Staf_HSSE_MOR_VII:

                $id_request = "HSSE_STATUS";

            break;
            case $this->Reviewer_Vendor:

            

            break;
            default :
            if (($role >= 22) && ($role <= 32) || ($role == 34) || ($role == 35)){
                
                

            }
            
            else if (($role >= 8) && ($role <= 18) || ($role == 20) || ($role == 21)){
                
                
                
            }
            
            break;

        }
        

        $result = mysqli_query($kon, "SELECT $id_request FROM `tr_contract` WHERE `tr_contract`.`CONTRACT_ID` = $id_contract");

        if ($result){
            while ($row = mysqli_fetch_array($result)) {
             //   $data = array($id_request=>);

                echo $row[$id_request];
            }
        }
        else{
            echo "Error";
        }

    }

    function test_compile() {

        $contract_id = $this->input->get('contract');

        $user_id = $this->input->get('user');

        $this->do_compile($contract_id, $user_id, $this->SIGNATURE);

    }



    function set_compile() {

        $content = trim(file_get_contents("php://input"));

        $decoded = json_decode($content);

        $contract_id = $decoded->id_contract;

        $user_id = $decoded->id_user;

        $signature = $decoded->signature;

        $this->do_compile($contract_id, $user_id, $signature);

    }



    function intepret_format_page($value){

        switch($value){

            case 'PAGE':

            $table_page = 'Halaman {PAGENO} dari {nbpg}';

            return $table_page;

            case 'SIGN':

            $table_sign = '<table width="100%" border="1" style="border-collapse: collapse; vertical-align: bottom; font-family: sans-serif; font-size: 9pt;">

            <tr><td colspan="2" style="text-align: center;">PARAF</td></tr>

            <tr><td style="text-align: center;">PERUSAHAAN</td>

            <td style="text-align: center;">KONTRAKTOR</td></tr>

            <tr><td style="text-align: center;"><br>&nbsp;<br></td>

            <td style="text-align: center;"><br>&nbsp;<br></td>

            </tr></table>';

            return $table_sign;

            default:

            return $value;

        }

    }



    function do_compile($contract_id, $user_id, $signature) {

        $data_user = $this->M_user->get_by_id($user_id)->row();

        $data_contract = $this->M_contract->get_by_id($contract_id)->row();

        if (!$data_user) { echo json_encode(array("response" => -1)); }

        $role = $data_user->USER_ROLE;

        $compiled = $this->reset_content($contract_id);

        $date = date('Y-m-d H:i:s');

        switch ($role) {

            case $this->ROLE_VENDOR:

            $img = "<img src='data:image/jpeg;base64,".$signature."' style='height:100px'/>";

            $result = str_replace('[VENDOR_SIGNATURE]', $img, $compiled);

            $day_finance = $this->sla_duration($data_contract->FINANCE_DATETIME, $data_contract->PUBLISHED_DATETIME);

            $day_legal = $this->sla_duration($data_contract->LEGAL_DATETIME, $data_contract->PUBLISHED_DATETIME);

            $date_anchor = $data_contract->FINANCE_DATETIME;

            if ($day_legal > $day_finance) {

                $date_anchor = $data_contract->LEGAL_DATETIME;

            }

            $data = array(

                "vendor_certificate" => $this->STATUS_APPROVED,

                "vendor_signature" => $signature,

                "vendor_datetime" => $date,

                "vendor_sla" => $this->sla_duration($date, $date_anchor),

                'compiled' => $result,

            );

            $data_target = $this->M_user->get_by_id($data_contract->OFFICER_ID)->row();

            $this->fcm($data_contract->CONTRACT_TITLE, $data_target->TOKEN_DATA_1);



            $this->M_contract->set($contract_id, $data);



            break;

            case $this->ROLE_OFFICER:

            $img = "<img src='data:image/jpeg;base64,".$signature."' style='height:100px'/>";

            $result = str_replace('[OFFICER_SIGNATURE]', $img, $compiled);

            $data = array(

                "officer_certificate" => $this->STATUS_APPROVED,

                "officer_signature" => $signature,

                "officer_datetime" => $date,

                "officer_sla" => $this->sla_duration($date, $data_contract->VENDOR_DATETIME),

                'compiled' => $result,

            );



            $this->M_contract->set($contract_id, $data);



            $contract = $this->M_contract->get_by_id($contract_id)->row();

            $officer_id = $contract->OFFICER_ID;

            $template_id = $contract->TEMPLATE_ID;



            $template = $this->M_template->get_by_id($template_id)->row();



            $officer_data = $this->M_user->get_by_id($officer_id)->row();



            $array_officer_contract = $this->M_officer_contract->search(array('officer_id' => $officer_id))->result_array();



            $new_id = 0;;

            if (substr($officer_data->CREATED_ON, 0, 4) == substr($date, 0, 4)) {

                $new_id = $officer_data->COUNTEROFFICER;

            }

            foreach ($array_officer_contract as $officer_contract) {

                if ($officer_contract['COUNTER'] > $new_id

                    && substr($officer_contract['CREATED_ON'], 0, 4) == substr($date, 0, 4)) {

                    $new_id = $officer_contract['COUNTER'];

            }

        }



        $new_id++;



        $new_officer_contract = array(

            'officer_id' => $officer_id,

            'contract_id' => $contract_id,

            'counter' => $new_id

        );



        $resp = $this->M_officer_contract->add($new_officer_contract);

        $contract_code = $new_id;

        while (strlen($contract_code) < 3) {

            $contract_code = '0'.$contract_code;

        }



        $template_content = $template->TEMPLATE_CONTENT;

        $contract_content = $contract->CONTRACT_CONTENT;

        $signature_officer = $contract->OFFICER_SIGNATURE;

        $signature_vendor = $contract->VENDOR_SIGNATURE;



        $field = $this->M_field->get_active()->result();

        $contract_json = json_decode($contract_content, true);



        $duration = 0;

        foreach($field as $p) {

            $field_name = '['.$p->FIELD_NAME.']';

            if (strpos($template_content, $field_name) && isset($contract_json[$field_name])){

                switch ($field_name) {

                    case '[JANGKA_WAKTU_PELAKSANAAN]':

                    $duration = $contract_json[$field_name];

                    break;

                }

            }

        }



        foreach($field as $p) {

            $field_name = '['.$p->FIELD_NAME.']';

            if (strpos($template_content, $field_name) && isset($contract_json[$field_name])){

                $field_data = $contract_json[$field_name];

                switch ($field_name) {

                    case '[ADDENDUM_A]':

                    case '[ADDENDUM_B]':

                    case '[ADDENDUM_C]':

                    case '[ADDENDUM_D]':

                    case '[ADDENDUM_E]':

                    case '[ADDENDUM_F]':

                    case '[ADDENDUM_G]':

                    case '[ADDENDUM_H]':

                    $array = explode(';', $field_data);

                    $table_top = '<table width="100%" style="border-bottom: 0px solid #000000; vertical-align: bottom; font-family: sans-serif; font-size: 9pt;"><tr>

                    <td width="33%" style="text-align: left;">'.$this->intepret_format_page($array[0]).'</td>

                    <td width="33%" style="text-align: center;">'.$this->intepret_format_page($array[1]).'</td>

                    <td width="33%" style="text-align: right;">'.$this->intepret_format_page($array[2]).'</td>

                    </tr></table>';

                    $table_bottom = '<table width="100%" style="vertical-align: bottom; font-family: sans-serif; font-size: 9pt;"><tr>

                    <td width="33%" style="text-align: left;">'.$this->intepret_format_page($array[3]).'</td>

                    <td width="33%" style="text-align: center;">'.$this->intepret_format_page($array[4]).'</td>

                    <td width="33%" style="text-align: right;">'.$this->intepret_format_page($array[5]).'</td>

                    </tr></table>';

                    $headerHTML = '<htmlpageheader name="header'.$field_name.'" style="display:none">'

                    .$table_top.

                    '</htmlpageheader>

                    <pagebreak type="NEXT-ODD" resetpagenum="1" suppress="off"/>

                    <htmlpagefooter name="footer'.$field_name.'" style="display:none">'

                    .$table_bottom.

                    '</htmlpagefooter>

                    <setpageheader name="header'.$field_name.'" page="" value="on" show-this-page="1" />

                    <setpagefooter name="footer'.$field_name.'" page="" value="on" show-this-page="1" />';

                    $template_content = str_replace($field_name, $headerHTML, $template_content);

                    break;

                    case '[CONTRACT_DATE_CREATION]':

                                //$field_data = date('d - Y');

                                //$field_data = str_replace('-', $this->get_month(date('n')), $field_data);

                    $field_data = 'hari '.$this->get_day_name(date('N')).' tanggal '.date('d').' bulan '.$this->get_month(date('n')).' tahun '.date('Y');

                    if ($field_data !== '') { $template_content = str_replace($field_name, $field_data, $template_content); }

                    else { $template_content = str_replace($field_name, '____________________', $template_content); }

                    break;

                    case '[CONTRACT_NUMBER]':

                    $field_data = $contract_code.'/'.$officer_data->CODEOFFICER.'/'.date('Y').'-S0';

                    if ($field_data !== '') { $template_content = str_replace($field_name, $field_data, $template_content); }

                    else { $template_content = str_replace($field_name, '____________________', $template_content); }

                    break;

                    case '[TANGGAL_MULAI_PEKERJAAN]':

                    $dateTime = new DateTime();

                    $dateTime->modify("+3 day");

//                                $field_data = $dateTime->format('d - Y');

//                                $field_data = str_replace('-', $this->get_month($dateTime->format('n')), $field_data);

                    $field_data = 'hari '.$this->get_day_name($dateTime->format('N')).' tanggal '.$dateTime->format('d').' bulan '.$this->get_month($dateTime->format('n')).' tahun '.$dateTime->format('Y');

                    if ($field_data !== '') { $template_content = str_replace($field_name, $field_data, $template_content); }

                    else { $template_content = str_replace($field_name, '____________________', $template_content); }

                    break;

//                            case '[TANGGAL_SELESAI_PEKERJAAN]':

//                                $dateTime = new DateTime();

//                                $dateTime->modify('+'.($duration + 3)." day");

//                                $field_data = $dateTime->format('d - Y');

//                                $field_data = str_replace('-', $this->get_month($dateTime->format('n')), $field_data);

//                                $field_data = 'hari '.$this->get_day_name($dateTime->format('N')).' tanggal '.$dateTime->format('d').' bulan '.$this->get_month($dateTime->format('n')).' tahun '.$dateTime->format('Y');

//                                if ($field_data !== '') { $template_content = str_replace($field_name, $field_data, $template_content); }

//                                else { $template_content = str_replace($field_name, '____________________', $template_content); }

//                                break;

                    default:

                    if ($field_data !== '') { $template_content = str_replace($field_name, $field_data, $template_content); }

                    else { $template_content = str_replace($field_name, '____________________', $template_content); }

                    break;

                }

            }

        }



        if ($signature_officer != '' && strpos($template_content, '[OFFICER_SIGNATURE]')) {

            $img = "<img src='data:image/jpeg;base64,".$signature_officer."' style='height:100px'/>";

            $template_content = str_replace('[OFFICER_SIGNATURE]', $img, $template_content);

        }



        if ($signature_vendor != '' && strpos($template_content, '[VENDOR_SIGNATURE]')) {

            $img = "<img src='data:image/jpeg;base64,".$signature_vendor."' style='height:100px'/>";

            $template_content = str_replace('[VENDOR_SIGNATURE]', $img, $template_content);

        }



        $template_content = '<htmlpagefooter name="footerTemplate" style="display:none">

        <table width="100%" style="vertical-align: bottom; font-family: sans-serif; font-size: 9pt;">

        <tr><td width="33%" style="text-align: left;"></td>

        <td width="33%" style="text-align: center;">Halaman {PAGENO} dari {nbpg}</td>

        <td width="33%" style="text-align: right;"></td></tr></table></htmlpagefooter>

        <setpagefooter name="footerTemplate" page="" value="on" show-this-page="1" />'.$template_content;



        $this->M_contract->set($contract_id, array('compiled' => $template_content));



        break;

    }



    $this->create_pdf_by_id($contract_id);

    $this->set_api_log('set_compile', $user_id, 1);

    echo json_encode(array("response" => $this->db->affected_rows()));

}



function reset_contract(){

    $contract_id = $this->input->get('id');

    $reset_level = $this->input->get('level');

    if ($contract_id == '') {

        $content = trim(file_get_contents("php://input"));

        $decoded = json_decode($content);

        $contract_id = $decoded->doc_id;

        $reset_level = $decoded->reset_level;

    }



    switch ($reset_level) {

        case 0:

        $template_content = $this->reset_content($contract_id);

        $data = array(

            'compiled' => $template_content,

            'published' => '0',

            'legal_status' => '',

            'legal_note' => '',

            'finance_status' => '',

            'finance_note' => '',

            'officer_signature' => '',

            'officer_certificate' => '',

            'vendor_signature' => '',

            'vendor_certificate' => '',

        );

        $this->M_contract->set($contract_id, $data);

        $this->create_pdf_by_id($contract_id);

        break;

        case 1:

        $template_content = $this->reset_content($contract_id);

        $data = array(

            'compiled' => $template_content,

            'published' => '1',

            'legal_status' => '3',

            'legal_note' => '',

            'finance_status' => '3',

            'finance_note' => '',

            'officer_signature' => '',

            'officer_certificate' => '',

            'vendor_signature' => '',

            'vendor_certificate' => '',

        );

        $this->M_contract->set($contract_id, $data);

        $this->create_pdf_by_id($contract_id);

        break;

        case 2:

        $data = array(

            'published' => '1',

            'legal_status' => '3',

            'legal_note' => '',

            'finance_status' => '3',

            'finance_note' => '',

            'officer_signature' => $this->SIGNATURE,

            'officer_certificate' => '3',

            'vendor_signature' => $this->SIGNATURE,

            'vendor_certificate' => '3',

        );

        $this->M_contract->set($contract_id, $data);

        $template_content = $this->reset_content($contract_id);

        $data = array(

            'compiled' => $template_content,

        );

        $this->M_contract->set($contract_id, $data);

        $this->create_pdf_by_id($contract_id);

        break;

    }



    echo json_encode(array("response" => $this->db->affected_rows()));

}



function first_access(){

    $username = $this->input->get('username');

    $password = $this->input->get('password');

    $imei = $this->input->get('imei');

    if (!$username || !$password || !$imei) {

        $content = trim(file_get_contents("php://input"));

        $decoded = json_decode($content);

        $username = $decoded->username;

        $password = $decoded->password;

        $imei = $decoded->imei;

    }



    $data_user = $this->M_user->get_by_id($username, "USERNAME")->row();



    if (!$username || !$password) {

        echo json_encode(array("response" => -1, "id" => -1, "name" => "", "role" => -1, "imei" => -1));

    } else {

        $auth = 0;

        $isVerified = $this->M_user->verify_login($username, $password);

        if (($auth && isset($data_user)) || ($isVerified && isset($data_user))){

            if ($data_user->ROLE_ID <= 2 ){

                $this->set_api_log('first_access', $data_user->USER_ID, 0);

                echo json_encode(array("response" => 0, "id" => -1, "name" => "", "role" => -1, "imei" => -1));

            } else {

                if ($imei) {

                    $data = array('imei' => $imei);

                    $this->M_user->set($data_user->USER_ID, $data);

                }

                $this->set_api_log('first_access', $data_user->USER_ID, 1);

                echo json_encode(array("response" => 1, "id" => $data_user->USER_ID, "name" => $data_user->NAME, "role" => $data_user->USER_ROLE, "imei" => $data_user->IMEI));

            }

        } else {

            $this->set_api_log('first_access', '-1', 0);

            echo json_encode(array("response" => 0, "id" => -1, "name" => "", "role" => -1, "imei" => -1));

        }

    }

}



function check_imei() {

    $content = trim(file_get_contents("php://input"));

    $decoded = json_decode($content);

    $user_id = $decoded->id_user;

    $imei = $decoded->imei;



    $data_user = $this->M_user->get_by_id($user_id)->row();



    if ($imei == $data_user->IMEI) {

        $this->set_api_log('check_imei', $user_id, 1);

        echo json_encode(array("response" => 1));

    } else {

        $this->set_api_log('check_imei', $user_id, 0);

        echo json_encode(array("response" => -1));

    }

}



function set_api_log($uri, $id, $authorized){

    if ($uri == 'check_imei'

        || $uri == 'reset_contract'

        || $uri == 'set_imei'

        || $uri == 'set_token'

    ) {

        return;

}

$data = array(

    'URI' => $uri,

    'METHOD' => 'POST',

    'PARAMS' => $id,

    'API_KEY' => '',

    'IP_ADDRESS' => '',

    'TIME' => 1,

    'RTIME' => 1,

    'AUTHORIZED' => $authorized,

);

$this->M_api_log->add($data);

}



function reset_content($contract_id) {

    $contract = $this->M_contract->get_by_id($contract_id)->row();

    $template_id = $contract->TEMPLATE_ID;

    $template = $this->M_template->get_by_id($template_id)->row();



    $template_content = $template->TEMPLATE_CONTENT;

    $contract_content = $contract->CONTRACT_CONTENT;

    $signature_officer = $contract->OFFICER_SIGNATURE;

    $signature_vendor = $contract->VENDOR_SIGNATURE;



    $field = $this->M_field->get_active()->result();

    $contract_json = json_decode($contract_content, true);



    if ($signature_officer != '' && strpos($template_content, '[OFFICER_SIGNATURE]')) {

        $img = "<img src='data:image/jpeg;base64,".$signature_officer."' style='height:100px'/>";

        $template_content = str_replace('[OFFICER_SIGNATURE]', $img, $template_content);

    }



    if ($signature_vendor != '' && strpos($template_content, '[VENDOR_SIGNATURE]')) {

        $img = "<img src='data:image/jpeg;base64,".$signature_vendor."' style='height:100px'/>";

        $template_content = str_replace('[VENDOR_SIGNATURE]', $img, $template_content);

    }



    foreach($field as $p) {

        $field_name = '['.$p->FIELD_NAME.']';

        if (strpos($template_content, $field_name) && isset($contract_json[$field_name])){

            $field_data = $contract_json[$field_name];

            switch ($field_name) {

                case '[ADDENDUM_A]':

                case '[ADDENDUM_B]':

                case '[ADDENDUM_C]':

                case '[ADDENDUM_D]':

                case '[ADDENDUM_E]':

                case '[ADDENDUM_F]':

                case '[ADDENDUM_G]':

                case '[ADDENDUM_H]':

                $headerHTML = '<htmlpageheader name="header'.$field_name.'" style="display:none">

                <table width="100%" style="border-bottom: 0px solid #000000; vertical-align: bottom; font-family: sans-serif; font-size: 9pt;"><tr>

                <td width="33%" style="text-align: left;"></td>

                <td width="33%" style="text-align: center;"></td>

                <td width="33%" style="text-align: right;">'.$field_data.'</td>

                </tr></table>

                </htmlpageheader>

                <pagebreak type="NEXT-ODD" resetpagenum="1" suppress="off"/>

                <htmlpagefooter name="footer'.$field_name.'" style="display:none">

                <table width="100%" style="vertical-align: bottom; font-family: sans-serif; font-size: 9pt;">

                <tr><td width="33%" style="text-align: left;"></td>

                <table width="100%" border="1" style="border-collapse: collapse; vertical-align: bottom; font-family: sans-serif; font-size: 9pt;">

                <tr><td colspan="2" style="text-align: center;">PARAF</td></tr>

                <tr><td style="text-align: center;">PERUSAHAAN</td>

                <td style="text-align: center;">KONTRAKTOR</td></tr>

                <tr><td style="text-align: center;"><br>&nbsp;<br></td>

                <td style="text-align: center;"><br>&nbsp;<br></td></tr></table>

                <td width="33%" style="text-align: center;">Halaman {PAGENO} dari {nbpg}</td>

                <td width="33%" style="text-align: right;"></td></tr></table></htmlpagefooter>

                <setpageheader name="header'.$field_name.'" page="" value="on" show-this-page="1" />

                <setpagefooter name="footer'.$field_name.'" page="" value="on" show-this-page="1" />';

                $template_content = str_replace($field_name, $headerHTML, $template_content);

                break;

                default:

                if ($field_data !== '') { $template_content = str_replace($field_name, $field_data, $template_content); }

                else { $template_content = str_replace($field_name, '____________________', $template_content); }

                break;

            }

        }

    }



    $paging = '<htmlpagefooter name="footerTemplate" style="display:none">

    <table width="100%" style="vertical-align: bottom; font-family: sans-serif; font-size: 9pt;">

    <tr><td width="33%" style="text-align: left;"></td>

    <td width="33%" style="text-align: center;">Halaman {PAGENO} dari {nbpg}</td>

    <td width="33%" style="text-align: right;"></td></tr></table></htmlpagefooter>

    <setpagefooter name="footerTemplate" page="" value="on" show-this-page="1" />';

    $template_content = $paging.$template_content;



    return $template_content;

}



function get_day_name($value){

    switch ($value) {

        case 1: return 'Senin';

        case 2: return 'Selasa';

        case 3: return 'Rabu';

        case 4: return 'Kamis';

        case 5: return 'Jumat';

        case 6: return 'Sabtu';

    }

    return 'Minggu';

}



function get_month($value){

    switch ($value) {

        case 1: return 'Januari';

        case 2: return 'Februari';

        case 3: return 'Maret';

        case 4: return 'April';

        case 5: return 'Mei';

        case 6: return 'Juni';

        case 7: return 'Juli';

        case 8: return 'Agustus';

        case 9: return 'September';

        case 10: return 'Oktober';

        case 11: return 'November';

    }

    return 'Desember';

}



function create_pdf_by_id($contract_id){

    $data = $this->M_contract->get_by_id($contract_id)->row();

    $file_name = $data->CONTRACT_ID.'_'.$data->CONTRACT_TITLE;

    $margin_standard = 20;

    $format = array('orientation' => 'P',

        'mgl' => $margin_standard,

        'mgr' => $margin_standard,

        'mgt' => $margin_standard,

        'mgb' => $margin_standard + 10,

        'mgh' => $margin_standard * 0.5,

        'mgf' => $margin_standard * 0.5,

        'page_w' => 210,

        'page_h' => 297);

    $html_data = $data->COMPILED;

    $html_data = str_replace('jpeg;base64', 'jpg;base64', $html_data);

    $html_data = str_replace('[VENDOR_SIGNATURE]', '<br><br><br>', $html_data);

    $html_data = str_replace('[OFFICER_SIGNATURE]', '<br><br><br>', $html_data);

    $path = $this->create_pdf_custom($html_data, $file_name, '', $format, '', '', 'F');

    $path = site_url().'export/'.$file_name.'.pdf';

    return $path;

}



protected function create_pdf_custom($content = "", $filename='default', $orientation="", $format=false, $footer='', $header='', $mode='F') {

    $this->load->library('pdf');

    $pdfFilePath = FCPATH . "export/{$filename}.pdf";

    $m_pdf = new pdf('"utf-8","array(234,105)","","",10,5,5,10,6,3');

    $m_pdf->allow_charset_conversion = true;

    $m_pdf->charset_in = 'UTF-8';

    if ($format) {

            $m_pdf->pdf->AddPage($format['orientation'], // L - landscape, P - portrait

                '', '', '', '',

                $format['mgl'], // margin_left 88

                $format['mgr'], // margin right 5

                $format['mgt'], // margin top 10

                $format['mgb'], // margin bottom 0

                $format['mgh'], // margin header 0

                $format['mgf'], // margin footer 0

                "",

                '', '', '', '',

                  '', '', '', '', array($format['page_w'],$format['page_h'])); // page size 105, 234

        } else if ($orientation) $m_pdf->pdf->AddPage($orientation);

        $m_pdf->pdf->WriteHTML($content);

        $m_pdf->pdf->Output($pdfFilePath, 'F');

    }



    function sla_routine(){

        $this->M_contract->fields = array("CONTRACT_ID", "PUBLISHED", "PUBLISHED_DATETIME", "UPDATED_ON", "CONTRACT_TITLE", "LEGAL_ID", "LEGAL_STATUS", "LEGAL_DATETIME", "FINANCE_ID", "FINANCE_STATUS", "FINANCE_DATETIME", "VENDOR_ID", "VENDOR_DATETIME", "VENDOR_CERTIFICATE", "OFFICER_ID", "OFFICER_DATETIME", "OFFICER_CERTIFICATE");

        $contract = $this->M_contract->get_active()->result_array();

        foreach ($contract as $data_contract) {

            if ($data_contract['OFFICER_CERTIFICATE'] >= $this->STATUS_APPROVED

                || $data_contract['PUBLISHED'] < 1

            ) {

                continue;

        }

        else if ($data_contract['VENDOR_CERTIFICATE'] >= $this->STATUS_APPROVED) {

            $days = $this->sla_duration($data_contract['VENDOR_DATETIME'], '');

            if($days > $this->SLA_DELAY) {

                $data_user = $this->M_user->get_by_id($data_contract['OFFICER_ID'])->row();

                $this->fcm($data_contract['CONTRACT_TITLE'], $data_user->TOKEN_DATA_1, 'Contract Reminder');

            }

        }

        else if ($data_contract['LEGAL_STATUS'] >= $this->STATUS_APPROVED && $data_contract['FINANCE_STATUS'] >= $this->STATUS_APPROVED){

            $days1 = $this->sla_duration($data_contract['LEGAL_DATETIME'], '');

            $days2 = $this->sla_duration($data_contract['FINANCE_DATETIME'], '');

            if($days1 > $this->SLA_DELAY || $days2 > $this->SLA_DELAY) {

                $data_user = $this->M_user->get_by_id($data_contract['VENDOR_ID'])->row();

                $this->fcm($data_contract['CONTRACT_TITLE'], $data_user->TOKEN_DATA_1, 'Contract Reminder');

            }

        } else {

            if ($data_contract['FINANCE_STATUS'] <= $this->STATUS_PENDING){

                $days = $this->sla_duration($data_contract['PUBLISHED_DATETIME'], '');

                if($days > $this->SLA_DELAY) {

                    $data_user = $this->M_user->get_by_id($data_contract['FINANCE_ID'])->row();

                    $this->fcm($data_contract['CONTRACT_TITLE'], $data_user->TOKEN_DATA_1, 'Contract Reminder');

                }

            }

            if ($data_contract['LEGAL_STATUS'] <= $this->STATUS_PENDING){

                $days = $this->sla_duration($data_contract['PUBLISHED_DATETIME'], '');

                if($days > $this->SLA_DELAY) {

                    $data_user = $this->M_user->get_by_id($data_contract['LEGAL_ID'])->row();

                    $this->fcm($data_contract['CONTRACT_TITLE'], $data_user->TOKEN_DATA_1, 'Contract Reminder');

                }

            }

        }

    }

}



function sla_duration_test(){

    echo $this->sla_duration('2018-05-05 22:00:00', '2018-05-0 22:00:00').'=================';

}



function sla_duration($datetime1, $datetime2){

    $date1 = new DateTime($datetime1);

    if ($datetime2 != '') { $date2 = new DateTime($datetime2); }

    else { $date2 = new DateTime(); }

    $days  = $date2->diff($date1)->format('%a');

    return $days;

}



function fcm($contract_title, $registrationIds, $title = 'New Contract'){

    if ($registrationIds) {

        $msg = array(

            'title' => $title,

            'body' => $contract_title,

            'sound' => 'default'

        );

        $fields = array(

            'to' => $registrationIds,

            'notification' => $msg

        );

        $headers = array(

            'Authorization: key=AAAAVRQsryE:APA91bF--l48QBuXmNSKffZKBeKKHmlxONisL8242u6oX-4UM2PufkoZMG5NxFBh5IBftAO3qsJrSd5vp_ChgXWOk-NLx_1cy0LNKDoABdiat9d22jeX9j6tzP90AJ5JR7oEQ65wYIi0',

            'Content-Type: application/json'

        );



            #Send Reponse To FireBase Server

        $ch = curl_init();

        curl_setopt($ch, CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send' );

        curl_setopt($ch, CURLOPT_POST, true );

        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers );

        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true );

        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false );

        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode( $fields ) );

        $result = curl_exec($ch );

        curl_close( $ch );

        return $result;

    }

    return '';

}



private $SIGNATURE = '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

';


}
