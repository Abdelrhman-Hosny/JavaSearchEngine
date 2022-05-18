import {Component, Input, OnInit} from '@angular/core';
import {Router} from'@angular/router'
import {NgForm} from "@angular/forms";
@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  @Input() value :string | any;
  constructor(private router:Router) { }

  ngOnInit(): void {
  }
  search():void
  {

    this.router.navigateByUrl('/results',{state:{term:this.value}}).then();
  }
acceptdata(data:any)
{
this.value=data;
}
}
