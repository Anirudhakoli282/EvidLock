import { Component } from '@angular/core';

import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { LogoutModalComponent } from '../../_LOGIN/logout-modal/logout-modal.component';
import { AuthService } from '../../../service/auth/auth.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  role?: string;

  constructor(
    private modalService: NgbModal,
    private auth: AuthService) { }

  ngOnInit(): void {
    this.role = this.auth.getRole() as string | undefined;
  }

  logout() {
    const modalRef = this.modalService.open(LogoutModalComponent, {
      size: 'md'
    });
  }
}
