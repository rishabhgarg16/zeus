// pages/privacy.js
import React from 'react';
import Layout from '../components/Layout';

export default function PrivacyPolicy() {
  return (
    <Layout>
      {/* Hero Section */}
      <div className="bg-indigo-600 text-white py-12">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h1 className="text-3xl font-bold">Privacy Policy</h1>
          <p className="mt-2">Last Updated: February 25, 2025</p>
        </div>
      </div>

      {/* Content Section */}
      <div className="py-12">
        <div className="max-w-4xl mx-auto px-4">
          <div className="prose prose-lg">
            <p>This Privacy Policy describes how HIT11 ("we," "our," or "us") collects, uses, and discloses information about you when you use our mobile application, website, and related services (collectively, the "Service").</p>

            <h2>1. INFORMATION WE COLLECT</h2>

            <h3>1.1 Information You Provide to Us</h3>
            <p>We collect information you provide directly to us, including:</p>
            <ul>
              <li><strong>Account Information:</strong> When you register for an account, we collect your name, email address, phone number, date of birth, and other information required to verify your identity.</li>
              <li><strong>Profile Information:</strong> Information you add to your profile, such as a profile picture, username, and bio.</li>
              <li><strong>Financial Information:</strong> Information necessary to process payments and withdrawals, such as bank account details and UPI IDs.</li>
              <li><strong>KYC Information:</strong> Documents and information required for KYC verification, such as identification documents and address proof.</li>
              <li><strong>Communications:</strong> Information you provide when you contact us for support or otherwise communicate with us.</li>
            </ul>

            <h3>1.2 Information We Collect Automatically</h3>
            <p>When you use our Service, we automatically collect certain information, including:</p>
            <ul>
              <li><strong>Device Information:</strong> Information about your device, including device type, operating system, unique device identifiers, IP address, and mobile network information.</li>
              <li><strong>Usage Information:</strong> Information about your interactions with the Service, including games played, time spent, and features used.</li>
              <li><strong>Location Information:</strong> General location information based on your IP address.</li>
            </ul>

            <h2>2. HOW WE USE YOUR INFORMATION</h2>
            <p>We use the information we collect to:</p>
            <ul>
              <li>Provide, maintain, and improve the Service</li>
              <li>Process transactions and send related information</li>
              <li>Verify your identity and prevent fraud</li>
              <li>Send you technical notices, updates, security alerts, and support messages</li>
              <li>Respond to your comments, questions, and customer service requests</li>
              <li>Monitor and analyze trends, usage, and activities in connection with the Service</li>
              <li>Personalize your experience on the Service</li>
              <li>Comply with legal obligations</li>
            </ul>

            <h2>3. HOW WE SHARE YOUR INFORMATION</h2>
            <p>We may share your information in the following circumstances:</p>
            <ul>
              <li><strong>With Service Providers:</strong> We share information with third-party service providers who help us operate the Service, such as payment processors, cloud storage providers, and customer support services.</li>
              <li><strong>For Legal Reasons:</strong> We may share information if we believe disclosure is necessary to comply with applicable laws, regulations, legal processes, or governmental requests.</li>
              <li><strong>In Connection with Business Transfers:</strong> We may share information in connection with a merger, acquisition, or sale of all or a portion of our business.</li>
              <li><strong>With Your Consent:</strong> We may share information with third parties when you give us consent to do so.</li>
            </ul>

            <h2>4. DATA SECURITY</h2>
            <p>We implement appropriate technical and organizational measures to protect the security of your personal information. However, no method of transmission over the Internet or electronic storage is 100% secure. Therefore, while we strive to use commercially acceptable means to protect your personal information, we cannot guarantee its absolute security.</p>

            <h2>5. YOUR CHOICES</h2>
            <p>You can access and update certain information about your account by logging into your account settings. You may also request deletion of your account by contacting us at privacy@hit11.com.</p>

            <h2>6. INFORMATION FOR USERS IN INDIA</h2>
            <p>As a user in India, you have certain rights under the Information Technology Act, 2000, and related rules. We process your personal information in accordance with these laws and provide appropriate safeguards for your data.</p>

            <h2>7. CHANGES TO THIS PRIVACY POLICY</h2>
            <p>We may update this Privacy Policy from time to time. If we make material changes, we will notify you through the Service or by other means, such as email. Your continued use of the Service after the effective date of the revised Privacy Policy constitutes your acceptance of the updated terms.</p>

            <h2>8. CONTACT US</h2>
            <p>If you have any questions about this Privacy Policy, please contact us at privacy@hit11.com.</p>
          </div>
        </div>
      </div>
    </Layout>
  );
}