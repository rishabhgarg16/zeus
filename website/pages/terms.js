// pages/terms.js
import React from 'react';
import Layout from '../components/Layout';

export default function TermsOfService() {
  return (
    <Layout>
      {/* Hero Section */}
      <div className="bg-indigo-600 text-white py-12">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h1 className="text-3xl font-bold">Terms of Service</h1>
          <p className="mt-2">Last Updated: February 25, 2025</p>
        </div>
      </div>

      {/* Content Section */}
      <div className="py-12">
        <div className="max-w-4xl mx-auto px-4">
          <div className="prose prose-lg">
            <h2>1. ACCEPTANCE OF TERMS</h2>
            <p>Welcome to HIT11. By downloading, accessing, or using our mobile application or website (collectively, the "Service"), you agree to be bound by these Terms of Service ("Terms"). If you do not agree to these Terms, please do not use the Service.</p>

            <h2>2. ELIGIBILITY</h2>
            <p>The Service is available only to individuals who are at least 18 years old. By using the Service, you represent and warrant that you are at least 18 years of age and are not prohibited from using the Service under applicable law.</p>

            <p>The Service is not available for use in states where skill gaming is prohibited by law, including but not limited to Assam, Andhra Pradesh, Odisha, Telangana, Nagaland, Sikkim, and Tamil Nadu. By using the Service, you confirm that you are not accessing or using the Service from these restricted regions.</p>

            <h2>3. SKILL-BASED GAMING</h2>
            <p>HIT11 offers skill-based games only. Success in these games is determined predominantly by the user's knowledge, judgment, attention, and experience. While elements of chance may exist, skill is the predominant factor in determining the outcome.</p>

            <h2>4. ACCOUNT REGISTRATION</h2>
            <p>To use certain features of the Service, you must register for an account. You agree to provide accurate, current, and complete information during the registration process and to update such information to keep it accurate, current, and complete.</p>

            <p>HIT11 reserves the right to suspend or terminate your account if any information provided proves to be inaccurate, not current, or incomplete. You are responsible for maintaining the confidentiality of your account and password and for restricting access to your account.</p>

            <h2>5. USER CONDUCT</h2>
            <p>You agree not to:</p>
            <ul>
              <li>Use the Service for any illegal purpose</li>
              <li>Attempt to manipulate the outcomes of games</li>
              <li>Use multiple accounts</li>
              <li>Use automated systems or software to interact with the Service</li>
              <li>Engage in any activity that interferes with or disrupts the Service</li>
              <li>Impersonate any person or entity</li>
            </ul>

            <h2>6. FINANCIAL TERMS</h2>
            <p>6.1 <strong>Deposits:</strong> All deposits are securely processed through our payment partners.</p>
            <p>6.2 <strong>Withdrawals:</strong> Withdrawals will be processed within 24-48 hours, subject to verification.</p>
            <p>6.3 <strong>KYC Requirements:</strong> For withdrawals, users may be required to complete KYC (Know Your Customer) verification in compliance with applicable laws and regulations.</p>

            <h2>7. INTELLECTUAL PROPERTY</h2>
            <p>All content included on the Service, including but not limited to text, graphics, logos, icons, images, audio clips, and software, is the property of HIT11 or its content suppliers and is protected by copyright, trademark, and other intellectual property laws.</p>

            <h2>8. PRIVACY</h2>
            <p>Your privacy is important to us. Our Privacy Policy describes how we collect, use, and disclose information about you.</p>

            <h2>9. DISCLAIMERS</h2>
            <p>THE SERVICE IS PROVIDED ON AN "AS IS" AND "AS AVAILABLE" BASIS. HIT11 EXPRESSLY DISCLAIMS ALL WARRANTIES OF ANY KIND, WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT.</p>

            <h2>10. LIMITATION OF LIABILITY</h2>
            <p>TO THE MAXIMUM EXTENT PERMITTED BY LAW, HIT11 SHALL NOT BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, OR PUNITIVE DAMAGES, OR ANY LOSS OF PROFITS OR REVENUES, WHETHER INCURRED DIRECTLY OR INDIRECTLY.</p>

            <h2>11. MODIFICATIONS TO THE SERVICE AND TERMS</h2>
            <p>HIT11 reserves the right to modify or discontinue, temporarily or permanently, the Service or any features or portions thereof without prior notice. We also reserve the right to update these Terms at any time. Your continued use of the Service after such modifications will constitute your acknowledgment of the modified Terms.</p>

            <h2>12. TERMINATION</h2>
            <p>HIT11 may terminate or suspend your account and bar access to the Service immediately, without prior notice or liability, for any reason whatsoever, including without limitation if you breach the Terms.</p>

            <h2>13. GOVERNING LAW</h2>
            <p>These Terms shall be governed by the laws of India, without respect to its conflict of laws principles. Any dispute arising under these Terms shall be subject to the exclusive jurisdiction of the courts in Mumbai, Maharashtra, India.</p>

            <h2>14. CONTACT US</h2>
            <p>If you have any questions about these Terms, please contact us at legal@hit11.com.</p>
          </div>
        </div>
      </div>
    </Layout>
  );
}