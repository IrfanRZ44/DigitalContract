<?php defined('BASEPATH') OR exit('No direct script access allowed');

class Json_sender2 extends CI_Controller {
    private $STATUS_UNREAD = 0;
    private $STATUS_PENDING = 1;
    private $STATUS_REJECTED = 2;
    private $STATUS_APPROVED = 3;
    
    private $ROLE_OFFICER = 5;
    private $ROLE_VENDOR = 6;

    public function __construct() {
        parent::__construct();
        $this->load->model('M_contract');
        $this->load->model('M_user');
        $this->load->model('M_template');
        $this->load->model('M_field');
        $this->load->model('M_pdf');
        $this->load->model('M_officer_contract');
        $this->load->library('phpmailer'); // load library
    }

    function get_token() {
    
      // $nik        = $this->input->get('nik');
        // $contract = $this->input->post('contract');
        $client_id = '15542010';//'16314426';
        $client_secret = 'u2jk-j759-h50i-66hj';//'39j1-sifb-del9-3cg6';

        $url = "https://esign.idtrust.co.id/oauth/token?client_id=".$client_id."&client_secret=".$client_secret."&grant_type=client_credentials";//"https://esign-dev.bssn.go.id/oauth/token?client_id=".$client_id."&client_secret=".$client_secret."&grant_type=client_credentials";

        //open connection
        $ch = curl_init();

        //set the url, number of POST vars, POST data
        curl_setopt($ch,CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch,CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch,CURLOPT_HEADER, false);
        curl_setopt($ch,CURLOPT_SSL_VERIFYPEER, 0);

        //execute post
        $result = curl_exec($ch);
        $info = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $errno = curl_errno($ch);
        curl_close($ch);

        if($errno) {
            echo json_encode(array("message"=>"Error get access token"));
        }
        else{
            $result = json_decode($result);
            if($info==200){
              
              $this->upload_document($result->access_token);  
            } 
            else {
              echo json_encode(array("message"=>$result->error));
            }
        }
    }

    function upload_document($token){
        $token = $token;

        $endpoint = 'https://esign.idtrust.co.id/api/v2/entity/sign/request';//'https://esign-dev.bssn.go.id/api/v2/entity/sign/request';

        $content = trim(file_get_contents("php://input"));
        $decoded = json_decode($content);
        $contract_title = $decoded->contract_title;
        $contract_path = $decoded->contract_path;
        $passphrase = $decoded->passphrase;
        $id_user = $decoded->id_user;
        $id_contract = $decoded->id_contract;


        $data_user        = $this->M_user->get_id($id_user)->row();
        $penandatangan = $data_user->NIK;//$decoded->penandatangan;

        $params = array('penandatangan' => $penandatangan,
                        'tampilan'      => 'visible',
                        'image'         => 'false',
                        'linkQR'        => 'http://digitalcontractv3.kabirland.technology',
                        'halaman'       => 'terakhir',
                        'yAxis'         => '60.77',
                        'xAxis'         => '95.10',
                        'width'         => '551.64',
                        'height'        => '152.78');
        $url = $endpoint . '?' . http_build_query($params);

        $header = array(
                    'Authorization: Bearer '.$token,
                    'Content-Type:multipart/form-data',
                  );

        $contract_id          = $id_contract;
        $user_id              = $id_user;
        $contract_title       = $contract_id."_".$contract_title;
        $contract_path       = $contract_path;
        $data_contract        = $this->M_contract->get_by_id($contract_id)->row();
        $reviewer_status      = $data_contract->REVIEWER_STATUS;
        $legal_status         = $data_contract->LEGAL_STATUS;
        $finance_status       = $data_contract->FINANCE_STATUS;
        $vendor_certificate   = $data_contract->VENDOR_CERTIFICATE; 
        $officer_certificate  = $data_contract->OFFICER_CERTIFICATE; 
        $hsse_status          = $data_contract->HSSE_STATUS;

        //finish - tambahan
        if (($reviewer_status=="5")&&($legal_status=="5")&&($finance_status=="5")&&($hsse_status=="5")&&($officer_certificate!="3")) {
            
            $this->do_compile($contract_id);
            $files = array(
                      realpath($_SERVER['DOCUMENT_ROOT'])."/export/".$contract_path.".pdf",
                    );
                    
        }else if (($reviewer_status=="5")&&($legal_status=="5")&&($finance_status=="5")&&($hsse_status=="5")&&($officer_certificate=="3")) {
          $files = array(
                    realpath($_SERVER['DOCUMENT_ROOT'])."/export_signed/".$contract_path.".pdf",
                  );
        }

        $postfields = array();

        foreach ($files as $index => $file) {
          if (function_exists('curl_file_create')) { // For PHP 5.5+
            $file = curl_file_create($file);
          } else {
            $file = '@' . realpath($file);
          }
          $postfields["file"] = $file;
        }

        //open connection
        $ch = curl_init();

        //set the url, number of POST vars, POST data
        curl_setopt($ch,CURLOPT_URL, $url);
        curl_setopt($ch,CURLOPT_HTTPHEADER, $header);
        curl_setopt($ch,CURLOPT_POST, true);
        // curl_setopt($ch,CURLOPT_POSTFIELDS,$fields);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $postfields);
        curl_setopt($ch,CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch,CURLOPT_SSL_VERIFYPEER, 0);
        curl_setopt($ch,CURLOPT_CUSTOMREQUEST, "POST");
        // curl_setopt($ch,CURLOPT_SAFE_UPLOAD, false);

        //execute post
        $result = curl_exec($ch);
        $info = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $errno = curl_errno($ch);
        curl_close($ch);

        if($errno) {
            echo json_encode(array("message"=>"Error uploading file"));
            echo realpath($_SERVER['DOCUMENT_ROOT'])."/export/".$contract_title.".pdf";
        }
        else{
          $result = json_decode($result);
          if($info==200){
              $this->signing($token,$result->id_signed);
          }
          else {
            echo json_encode(array("message"=>$result->error."saya"));
          }
        }
    }

    function signing($token,$id_document){
      $token      = $token;
      $id_document= $id_document;
      $content = trim(file_get_contents("php://input"));
        $decoded = json_decode($content);
        $contract_title = $decoded->contract_title;
        $contract_path = $decoded->contract_path;
        $passphrase = $decoded->passphrase;
        $id_user = $decoded->id_user;
        $id_contract = $decoded->id_contract;

        // $passphrase = $this->input->get('passphrase');
        // $id_user = $this->input->get('id_user');
        // $id_contract = $this->input->get('id_contract');
        // $contract_title = $this->input->get('contract_title');
        // $contract_path = $this->input->get('contract_path');    

      $endpoint = 'https://esign.idtrust.co.id/api/v2/entity/sign/';//'https://esign-dev.bssn.go.id/api/v2/entity/sign/';
      $params = array('passphrase'    => $passphrase,
                      'approved_info' => 'ok');
      $url = $endpoint . $id_document. '?' . http_build_query($params);

      $header = array(
                  'Authorization: Bearer '.$token,
                );

      //open connection
      $ch = curl_init();

      //set the url, number of POST vars, POST data
      curl_setopt($ch,CURLOPT_URL, $url);
      curl_setopt($ch,CURLOPT_HTTPHEADER, $header);
      curl_setopt($ch,CURLOPT_POST, true);
      curl_setopt($ch,CURLOPT_RETURNTRANSFER, true);
      curl_setopt($ch,CURLOPT_SSL_VERIFYPEER, 0);
      curl_setopt($ch,CURLOPT_CUSTOMREQUEST, "POST");

      //execute post
      $result = curl_exec($ch);
      $info = curl_getinfo($ch, CURLINFO_HTTP_CODE);
      $errno = curl_errno($ch);
      curl_close($ch);

      if($errno) {
          echo json_encode(array("message"=>"Error signing file"));
          $this->hapusNomorContract();
      }
      else{
        if($info==200){
              $this->downloadDocument($token,$id_document);
        } 
        else {
          $result = json_decode($result);
          echo json_encode(array("message"=>$result->error));
      $this->hapusNomorContract();
        }
      }
    }

    function hapusNomorContract(){
      $id_contract = $this->input->get('id_contract');

      $host = "localhost";
      $user = "u7431121_mor7com_digital_contractv3";
      $password = "u7431121_mor7com_digital_contractv3";
      $namaDb = "u7431121_mor7com_digital_contractv3";
      $kon = mysqli_connect($host, $user, $password, $namaDb);

      $result = mysqli_query($kon, "SELECT `OFFICER_CONTRACT_ID` FROM `tr_officer_contract` WHERE CONTRACT_ID='$id_contract' ORDER BY COUNTER DESC LIMIT 1");

      while ($row = mysqli_fetch_array($result)) {
       $idResult = $row['OFFICER_CONTRACT_ID'];
      
       $delete = mysqli_query($kon, "DELETE FROM `tr_officer_contract` WHERE OFFICER_CONTRACT_ID='$idResult'");
      }

    }
    
    function testes(){
      $host = "localhost";
      $user = "mor7com_dolby";
      $password = "mor7com_dolby";
      $namaDb = "mor7com_dolby";
      $kon = mysqli_connect($host, $user, $password, $namaDb);

      $req = $_GET['req'];
        if (isset($_GET['req'])) {
          $req = $_GET['req'];
          if ($req == 'cek_user') {
            $result = mysqli_query($kon, "SELECT * FROM users");

            if ($result) {
              $data_user = array();
              while ($row = mysqli_fetch_array($result)) {
                $row_array['username'] = $row['username'];
                $row_array['password'] = $row['password'];

                array_push($data_user,$row_array);
              }
              echo json_encode($data_user);
            }
            else{
              echo json_encode(array("response" => "Failed"));
            }
            }
        }
    }

    function getReviewer(){
      $content = trim(file_get_contents("php://input"));
      $decoded = json_decode($content);
      $role_user = $decoded->role_user;
      $id_contract = $decoded->id_contract;

      $host = "localhost";
      $user = "mor7com_digitalcontractv3";
      $password = "mor7com_digitalcontractv3";
      $namaDb = "mor7com_digitalcontractv3";
      $kon = mysqli_connect($host, $user, $password, $namaDb);

      $result = mysqli_query($kon, "SELECT * FROM `user` WHERE USER_ROLE = '$role_user'");
      
      $arrayHasil = [];
       
      while ($row = mysqli_fetch_array($result)) { 
        $data = array("username"=>$row['USERNAME']
        , "name"=>$row['NAME']
        , "user_id"=>$row['USER_ID']
        , "email"=>$row['EMAIL']
        , "nik"=>$row['NIK']
        , "id_contract"=>$id_contract
        );
        
        array_push($arrayHasil, $data);
      }
      echo json_encode($arrayHasil);
    }
    
    function setReviewer(){
      $content = trim(file_get_contents("php://input"));
      $decoded = json_decode($content);
      $id_user = $decoded->id_user;
      $id_contract = $decoded->id_contract;
      $jenis_mgr = $decoded->jenis_mgr;

      $host = "localhost";
      $user = "mor7com_digitalcontractv3";
      $password = "mor7com_digitalcontractv3";
      $namaDb = "mor7com_digitalcontractv3";
      $kon = mysqli_connect($host, $user, $password, $namaDb);

      $result = mysqli_query($kon, "UPDATE `tr_contract` SET `$jenis_mgr` = '$id_user' WHERE `tr_contract`.`CONTRACT_ID` = '$id_contract'");
      
      if ($result) {
        echo "Success";
      }else{
        echo $result;
      }
    }
    
    function cekContractReviewer(){
      $content = trim(file_get_contents("php://input"));
      $decoded = json_decode($content);
      $id_contract = $decoded->id_contract;
      $jenis_mgr = $decoded->jenis_mgr;
      $jenis_mgr2 = $decoded->jenis_mgr2;

      $host = "localhost";
      $user = "mor7com_digitalcontractv3";
      $password = "mor7com_digitalcontractv3";
      $namaDb = "mor7com_digitalcontractv3";
      $kon = mysqli_connect($host, $user, $password, $namaDb);

      $result = mysqli_query($kon, "SELECT * FROM `tr_contract` WHERE `tr_contract`.`CONTRACT_ID` = '$id_contract'");
      
      $data1 = 0;
      $data2 = 0;

      while ($row = mysqli_fetch_array($result)) {
          $data1 = $row[$jenis_mgr];

          if (isset($jenis_mgr2) || jenis_mgr2 != "") {
            $data2 = $row[$jenis_mgr2];
          }
          echo json_encode($data1 + "___" + $data2);
          // if ($data == 0){
          // }
          // else{
              // 
          // }
          // echo json_encode(array("result"=>"Reviewed"));
      }
    }

    function downloadDocument($token,$id_document){
      $token        = $token;
      $id_document  = $id_document;
      $url          = 'https://esign.idtrust.co.id/api/v2/entity/sign/download/'.$id_document;//'https://esign-dev.bssn.go.id/api/v2/entity/sign/download/'.$id_document;

      $header       = array('Authorization: Bearer '.$token,);

      //open connection
      $ch = curl_init();

      //set the url, number of POST vars, POST data
      curl_setopt($ch,CURLOPT_URL, $url);
      curl_setopt($ch,CURLOPT_HTTPHEADER, $header);
      curl_setopt($ch,CURLOPT_RETURNTRANSFER, true);
      curl_setopt($ch,CURLOPT_SSL_VERIFYPEER, 0);

      // curl_setopt($ch,CURLOPT_CUSTOMREQUEST, "GET");

      //execute post
      $result = curl_exec($ch);
      $info = curl_getinfo($ch, CURLINFO_HTTP_CODE);
      $errno = curl_errno($ch);
      curl_close($ch);

      if($errno) {
          echo json_encode(array("message"=>"Error downloading file"));
      }
      else{
        if($info==200) {
          //start - tambahan
        $content = trim(file_get_contents("php://input"));
        $decoded = json_decode($content);
        $contract_title = $decoded->contract_title;
        $contract_path = $decoded->contract_path;
        $passphrase = $decoded->passphrase;
        $user_id = $decoded->id_user;
        $contract_id = $decoded->id_contract;

      
          $contract_title = $contract_id."_".$contract_title;
          $contract_path = $contract_path;
          $data_contract = $this->M_contract->get_by_id($contract_id)->row();
          $reviewer_status = $data_contract->REVIEWER_STATUS;
          $legal_status = $data_contract->LEGAL_STATUS;
          $finance_status = $data_contract->FINANCE_STATUS;
          $vendor_certificate = $data_contract->VENDOR_CERTIFICATE; 
          $officer_certificate = $data_contract->OFFICER_CERTIFICATE; 
          $hsse_status = $data_contract->HSSE_STATUS; 

          //finish - tambahan
          if (($reviewer_status=="5")&&($legal_status=="5")&&($finance_status=="5")&&($hsse_status=="5")&&($officer_certificate!="3")) {
            $downloadPath = realpath($_SERVER['DOCUMENT_ROOT'])."/export_signed/".$contract_path.".pdf";
          }else if (($reviewer_status=="5")&&($legal_status=="5")&&($finance_status=="5")&&($hsse_status=="5")&&($officer_certificate=="3")) {
            $downloadPath = realpath($_SERVER['DOCUMENT_ROOT'])."/export_signed/".$contract_path."_final.pdf";
          }

          /*$downloadPath = realpath($_SERVER['DOCUMENT_ROOT'])."/export_signed/87_testing11_final.pdf";*/

          $file = fopen($downloadPath, "w+");

          fputs($file, $result);

          /*echo json_encode(array('response'=>'1'));*/

          //start - tambahan
          /*$content = trim(file_get_contents("php://input"));
          $decoded = json_decode($content);
          $contract_id = $decoded->id_contract;
          $user_id = $decoded->id_user;*/

          // $data_user = $this->M_user->get_by_id($user_id)->row();

          // $data_contract = $this->M_contract->get_by_id($contract_id)->row();





          /*$contract_id = "73"; //CONTRACT_ID

          $user_id = "16"; //USER_ID*/

          $param1 = "3"; //status kontrak 2=reject/3=aprov

          $param2 = ""; //isi note kontrak jika direject

            // echo json_encode(array("message"=>"Success"));
          $this->set_status($contract_id, $user_id, $param1, $param2);

          //end - tambahan
        }
        else {
          $result = json_decode($result);
          echo json_encode(array("message"=>$result->error));
        }
      }
    }

    function send_notification(){
      $token = $this->input->post('token');
      $data['title'] = $this->input->post('title');
      $data['message'] = $this->input->post('message');
      $this->notification($token,$data);
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
        $response = curl_exec($ch);
        curl_close($ch);
    }
    //----------------------------------------------------------------

    function set_status($contract_id, $user_id, $param1, $param2) {
        /*$contract_id = "36"; //CONTRACT_ID
        $user_id = "11"; //USER_ID
        $param1 = "3"; //status kontrak 2=reject/3=aprov
        $param2 = ""; //isi note kontrak jika direject*/
        
        // Change the line below to your timezone!
    date_default_timezone_set('Asia/Makassar');
    $date = date("Y-m-d H:i:s");
        
        $data_user = $this->M_user->get_id($user_id)->row();
        $data_contract = $this->M_contract->get_id($contract_id)->row();
        if (!$data_user) {
            echo json_encode(array("message" => "User not registered"));
            exit();
        }
        $role = $data_user->USER_ROLE; 
        $date = date('Y-m-d H:i:s');
        
        switch ($role) {
            case $this->ROLE_VENDOR:
                if ($data_contract->VENDOR_CERTIFICATE == $this->STATUS_APPROVED) {
                    $data = array(
                        "vendor_certificate" => 5,
                        "vendor_signature" => $param2,
                        "vendor_datetime" => $date,
                    );
                }
                break;
            case $this->ROLE_OFFICER:
                if ($data_contract->OFFICER_CERTIFICATE < $this->STATUS_APPROVED) {
                    $data = array(
                        "officer_certificate" => $param1,
                        "officer_signature" => $param2,
                        "officer_datetime" => $date,
                    );
                }
                break;
        }
        
        $this->M_contract->set($contract_id, $data); 
        echo json_encode(array("response" => $this->db->affected_rows()));

        //tambahan tidak harus berhasil
        $this->cek_notifikasi($contract_id);
    }

    function cek_notifikasi($contract_id){
        //------------------------------------------------
            $data = $this->M_contract->get_by_id($contract_id)->row();
            $contract_title = $data->CONTRACT_TITLE;
            $status_legal = $data->LEGAL_STATUS;
            $status_reviewer = $data->REVIEWER_STATUS;
            $status_finance = $data->FINANCE_STATUS;
            $status_hsse = $data->HSSE_STATUS;
            $status_vendor = $data->VENDOR_CERTIFICATE;
            $status_officer = $data->OFFICER_CERTIFICATE;

            $legal_id = $data->LEGAL_ID;
            $reviewer_id = $data->REVIEWER_ID;
            $finance_id = $data->FINANCE_ID;
            $vendor_id = $data->VENDOR_ID;
            $officer_id = $data->OFFICER_ID;

            /*$data_legal = $this->M_user->get_id($legal_id)->row();
            $email_legal = $data_legal->EMAIL;
            $this->send_email($email_legal, 'Admin Digital Contract', 'Ada kontrak baru untuk anda review : '.$contract_title);
            $data_reviewer = $this->M_user->get_id($reviewer_id)->row();
            $email_reviewer = $data_reviewer->EMAIL;
            $this->send_email($email_reviewer, 'Admin Digital Contract', 'Ada kontrak baru untuk anda review : '.$contract_title);
            $data_finance = $this->M_user->get_id($finance_id)->row();
            $email_finance = $data_finance->EMAIL;
            $this->send_email($email_finance, 'Admin Digital Contract', 'Ada kontrak baru untuk anda review : '.$contract_title);*/
            /*$data_vendor = $this->M_user->get_id($vendor_id)->row();
            $email_vendor = $data_vendor->EMAIL;
            $data_officer = $this->M_user->get_id($officer_id)->row();
            $email_officer = $data_officer->EMAIL;*/
            /*$this->send_email($email, 'Admin Digital Contract', 'Ada kontrak baru untuk anda review.');*/
            
            if ( ($status_legal=='5') && ($status_reviewer=='5') && ($status_finance=='5') && ($status_vendor=='5') && ($status_hsse=='5') && ($status_officer!='3') ) {
                $data_vendor = $this->M_user->get_id($vendor_id)->row();
                $email_vendor = $data_vendor->EMAIL;

                $data_body['title'] = "Digital Contract";
                $data_body['message'] = 'Yth. Vendor, Anda punya kontrak baru untuk ditandatangani : '.$contract_title;
                $token_vendor = $data_vendor->TOKEN_DATA_1;
                $this->notification($token_vendor, $data_body);

                $this->send_email($email_vendor, 'Admin Digital Contract', 'Yth. Vendor, Ada kontrak baru untuk anda tandatangani : '.$contract_title);
            }
            if ( ($status_legal=='5') && ($status_reviewer=='5') && ($status_finance=='5') && ($status_vendor=='5') && ($status_hsse=='5') && ($status_officer!='3') ) {
                $data_officer = $this->M_user->get_id($officer_id)->row();
                $email_officer = $data_officer->EMAIL;

                $data_body['title'] = "Digital Contract";
                $data_body['message'] = 'Yth. Officer, Anda punya kontrak baru untuk ditandatangani : '.$contract_title;
                $token_officer = $data_officer->TOKEN_DATA_1;
                $this->notification($token_officer, $data_body);

                $this->send_email($email_officer, 'Admin Digital Contract', 'Yth. Officer, Ada kontrak baru untuk anda tandatangani : '.$contract_title);
            }
            if ( ($status_legal=='5') && ($status_reviewer=='5') && ($status_finance=='5') && ($status_vendor=='5') && ($status_hsse=='5')  && ($status_officer=='3') ) {
                $data_vendor = $this->M_user->get_id($vendor_id)->row();
                $email_vendor = $data_vendor->EMAIL;
                $this->send_email($email_vendor, 'Admin Digital Contract', 'Yth. Vendor, Kontrak sudah selesai : '.$contract_title);
                $data_officer = $this->M_user->get_id($officer_id)->row();
                $email_officer = $data_officer->EMAIL;
                $this->send_email($email_officer, 'Admin Digital Contract', 'Yth. Officer, Kontrak sudah selesai : '.$contract_title);

                $data_body1['title'] = "Digital Contract";
                $data_body1['message'] = 'Yth. Vendor, Kontrak anda telah selesai : '.$contract_title;
                $data_body2['title'] = "Digital Contract";
                $data_body2['message'] = 'Yth. Officer, Kontrak anda telah selesai : '.$contract_title;
                $token_vendor = $data_vendor->TOKEN_DATA_1;
                $this->notification($token_vendor, $data_body1);
                $token_officer = $data_officer->TOKEN_DATA_1;
                $this->notification($token_officer, $data_body2);

            }
            
        //------------------------------------------------
    }

    function send_email($recipient, $subject, $message){
        $mail = new PHPMailer; 
        $mail->IsSMTP();
        $mail->SMTPSecure = 'ssl'; 
        $mail->Host = "mor7.com"; //host masing2 provider email
        // $mail->SMTPDebug = 2;
        $mail->SMTPDebug = false;
        $mail->Port = 465;
        $mail->SMTPAuth = true;
        $mail->Username = "pertaminadigitalcontract@mor7.com"; //user email
        $mail->Password = "admin"; //password email 
        $mail->SetFrom("pertaminadigitalcontract@mor7.com","Digital Contract Administrator"); //set email pengirim
        $mail->Subject = $subject; //subyek email
        $mail->AddAddress($recipient,"Yth.");  //tujuan email
        $mail->MsgHTML($message);
        $mail->Send();

        //ndak usah ada echo
        /*if($mail->Send()) echo "Message has been sent";
        else echo "Failed to sending message";*/
    }
    
    //----------------------------------------------------------------------
    function do_compile($contract_id) {

        $data_contract = $this->M_contract->get_by_id($contract_id)->row();

        $compiled = $this->reset_content($contract_id);

        $date = date('Y-m-d H:i:s');

        /*switch ($role) {*/
                

                $contract = $this->M_contract->get_by_id($contract_id)->row();

                $officer_id = $contract->OFFICER_ID;

                $template_id = $contract->TEMPLATE_ID;

                

                $template = $this->M_template->get_by_id($template_id)->row();

                

                $officer_data = $this->M_user->get_by_id($officer_id)->row();

                

                $array_officer_contract = $this->M_officer_contract->search(array('officer_id' => $officer_id))->result_array();


                $new_id = 0;

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

                $ven_id = $contract->VENDOR_ID;
                $off_id = $contract->OFFICER_ID;
                $vendor_data_ = $this->M_user->get_by_id($ven_id)->row();
                $officer_data_ = $this->M_user->get_by_id($off_id)->row();
                $signature_officer = $officer_data_->SIGNATURE;
                $signature_vendor = $vendor_data_->SIGNATURE;

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

                                $field_data = '/'.$officer_data->CODEOFFICER.'/'.date('Y').'-S0';

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

                

                /*break;

        }*/

        

        $this->create_pdf_by_id($contract_id);

        /*$this->set_api_log('set_compile', $user_id, 1);*/

        /*echo json_encode(array("response ok" => $this->db->affected_rows()));*/

    }
    
    function reset_content($contract_id) {

        $contract = $this->M_contract->get_by_id($contract_id)->row();

        $template_id = $contract->TEMPLATE_ID;

        $template = $this->M_template->get_by_id($template_id)->row();

        

        $template_content = $template->TEMPLATE_CONTENT;

        $contract_content = $contract->CONTRACT_CONTENT;

        /*$signature_officer = $contract->OFFICER_SIGNATURE;

        $signature_vendor = $contract->VENDOR_SIGNATURE;*/

        

        $field = $this->M_field->get_active()->result();

        $contract_json = json_decode($contract_content, true);

        

        /*if ($signature_officer != '' && strpos($template_content, '[OFFICER_SIGNATURE]')) {

            $img = "<img src='data:image/jpeg;base64,".$signature_officer."' style='height:100px'/>";

            $template_content = str_replace('[OFFICER_SIGNATURE]', $img, $template_content);

        }

        

        if ($signature_vendor != '' && strpos($template_content, '[VENDOR_SIGNATURE]')) {

            $img = "<img src='data:image/jpeg;base64,".$signature_vendor."' style='height:100px'/>";

            $template_content = str_replace('[VENDOR_SIGNATURE]', $img, $template_content);

        }*/

        

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

        $file_name = $data->PDF_PATH;

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

}

